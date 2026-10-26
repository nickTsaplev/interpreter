package ru.tsaplev.app

import org.junit.jupiter.api.assertTimeoutPreemptively
import ru.tsaplev.app.ASTreeNodes.ASTAssign
import ru.tsaplev.app.ASTreeNodes.ASTBinOP
import ru.tsaplev.app.ASTreeNodes.ASTConst
import ru.tsaplev.app.ASTreeNodes.ASTDo
import ru.tsaplev.app.ASTreeNodes.ASTFuncCall
import ru.tsaplev.app.ASTreeNodes.ASTIf
import ru.tsaplev.app.ASTreeNodes.ASTSeq
import ru.tsaplev.app.ASTreeNodes.ASTSkip
import ru.tsaplev.app.ASTreeNodes.ASTVar
import ru.tsaplev.app.ASTreeNodes.ASTWhile
import ru.tsaplev.app.execution.CompilationVisitor
import ru.tsaplev.app.execution.DataValue
import ru.tsaplev.app.execution.ExecutionVisitor
import ru.tsaplev.app.execution.DataIO
import ru.tsaplev.app.execution.optimize
import ru.tsaplev.app.execution.optimizeSequentialJumps
import ru.tsaplev.app.execution.stackMachine.MachineInstruction
import ru.tsaplev.app.execution.stackMachine.MachineInstruction.CONST
import ru.tsaplev.app.execution.stackMachine.MachineInstruction.JMP
import ru.tsaplev.app.execution.stackMachine.MachineInstruction.JNZ
import ru.tsaplev.app.execution.stackMachine.MachineInstruction.JZ
import ru.tsaplev.app.execution.stackMachine.MachineInstruction.LABEL
import ru.tsaplev.app.execution.stackMachine.MachineInstruction.WRITE
import ru.tsaplev.app.execution.stackMachine.StackMachine
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JumpOptimizationTest {
    private class TestIO : DataIO {
        val output = mutableListOf<Int>()
        override fun read(): DataValue = error("Unexpected read")
        override fun write(output: DataValue?, end: String) {
            this.output.add((output as DataValue.IntValue).value)
        }
    }

    private fun assertTargetsExist(instructions: List<MachineInstruction>) {
        val labels = instructions.filterIsInstance<LABEL>().map { it.label }.toSet()
        instructions.forEach {
            val target = when (it) {
                is JMP -> it.label
                is JZ -> it.label
                is JNZ -> it.label
                else -> null
            }
            if (target != null) assertTrue(target in labels, "Missing label: $target")
        }
    }

    private fun assertEquivalent(instructions: List<MachineInstruction>, expected: List<Int>) {
        val originalIO = TestIO()
        StackMachine().run(instructions, originalIO)
        assertEquals(expected, originalIO.output)
        for (optimized in listOf(optimizeSequentialJumps(instructions), optimize(instructions))) {
            assertTargetsExist(optimized)
            val optimizedIO = TestIO()
            StackMachine().run(optimized, optimizedIO)
            assertEquals(originalIO.output, optimizedIO.output)
        }
    }

    @Test
    fun unconditionalChainReachesFinalLabel() {
        val instructions = listOf(JMP("L1"), LABEL("L1"), JMP("L2"), LABEL("L2"), JMP("L3"),
            LABEL("L3"), CONST(42), WRITE())
        val optimized = optimizeSequentialJumps(instructions)
        assertEquals(listOf("L3"), optimized.filterIsInstance<LABEL>().map { it.label })
        assertTrue(optimized.filterIsInstance<JMP>().all { it.label == "L3" })
        assertEquivalent(instructions, listOf(42))
    }

    private fun conditionalChain(jump: MachineInstruction, condition: Int) = listOf(
        CONST(5), CONST(condition), jump, CONST(10), WRITE(), JMP("END"),
        LABEL("L1"), JMP("L2"), LABEL("L2"), CONST(20), WRITE(), LABEL("END"), WRITE()
    )

    @Test
    fun zeroJumpIsRedirectedAndStillConsumesCondition() {
        for (condition in listOf(0, 1)) {
            val instructions = conditionalChain(JZ("L1"), condition)
            assertEquals("L2", optimizeSequentialJumps(instructions).filterIsInstance<JZ>().single().label)
            assertEquivalent(instructions, listOf(if (condition == 0) 20 else 10, 5))
        }
    }

    @Test
    fun nonzeroJumpIsRedirectedAndStillConsumesCondition() {
        for (condition in listOf(0, -1)) {
            val instructions = conditionalChain(JNZ("L1"), condition)
            assertEquals("L2", optimizeSequentialJumps(instructions).filterIsInstance<JNZ>().single().label)
            assertEquivalent(instructions, listOf(if (condition != 0) 20 else 10, 5))
        }
    }

    @Test
    fun fallThroughAndMixedIncomingJumpsPreserveOutput() {
        val instructions = listOf(
            CONST(1), JZ("L1"), CONST(0), JNZ("L1"),
            LABEL("L1"), JMP("L2"), CONST(99), WRITE(),
            LABEL("L2"), CONST(7), WRITE(), CONST(0), JNZ("L1"), CONST(1), JZ("L1")
        )
        assertEquivalent(instructions, listOf(7))
    }

    @Test
    fun allJumpTypesCanShareOneRemovedLabel() {
        for ((zeroCondition, nonzeroCondition) in listOf(0 to 0, 1 to 1, 1 to 0)) {
            val instructions = listOf(
                CONST(zeroCondition), JZ("L1"), CONST(nonzeroCondition), JNZ("L1"), JMP("L1"),
                LABEL("L1"), JMP("L2"), LABEL("L2"), CONST(8), WRITE()
            )
            assertEquivalent(instructions, listOf(8))
        }
    }

    @Test
    fun selfJumpRemainsAnInfiniteLoop() {
        val instructions = listOf(LABEL("L1"), JMP("L1"))
        assertTimeoutPreemptively(Duration.ofSeconds(2)) {
            assertEquals(StackInstructionsToJSON(instructions), StackInstructionsToJSON(optimizeSequentialJumps(instructions)))
            assertEquals(StackInstructionsToJSON(instructions), StackInstructionsToJSON(optimize(instructions)))
            assertTargetsExist(optimize(instructions))
        }
    }

    @Test
    fun cyclicChainAndIncomingJumpsKeepValidTargets() {
        val instructions = listOf(JZ("ENTRY"), JNZ("L1"), LABEL("ENTRY"), JMP("L1"),
            LABEL("L1"), JMP("L2"), LABEL("L2"), JMP("L1"))
        assertTimeoutPreemptively(Duration.ofSeconds(2)) {
            assertEquals(StackInstructionsToJSON(instructions), StackInstructionsToJSON(optimizeSequentialJumps(instructions)))
            val optimized = optimize(instructions)
            assertTargetsExist(optimized)
            assertEquals(listOf("L1"), optimized.filterIsInstance<JMP>().map { it.label })
            assertEquals("L1", (optimized.last() as JMP).label)
        }
    }

    @Test
    fun compiledNestedConditionsAndLoopsMatchInterpreter() {
        fun constant(value: Int) = ASTConst(DataValue.IntValue(value))
        fun write(value: Int) = ASTFuncCall("write", constant(value))
        fun decrement() = ASTAssign("i", ASTBinOP("-", ASTVar("i"), constant(1)))
        fun positive() = ASTBinOP(">", ASTVar("i"), constant(0))
        fun writeAndDecrement() = ASTSeq(ASTFuncCall("write", ASTVar("i")), decrement())
        val programs = listOf(
            ASTIf(constant(1), ASTIf(constant(0), write(99), null), write(98)),
            ASTSeq(ASTAssign("i", constant(2)), ASTWhile(positive(), ASTWhile(positive(), writeAndDecrement()))),
            ASTSeq(ASTAssign("i", constant(2)), ASTDo(
                ASTSeq(ASTWhile(constant(0), ASTSkip()), writeAndDecrement()), positive()
            ))
        )
        for ((index, program) in programs.withIndex()) {
            val compiler = CompilationVisitor()
            program.visit(compiler)
            val directIO = TestIO()
            program.visit(ExecutionVisitor(directIO))
            assertEquals(listOf(emptyList(), listOf(2, 1), listOf(2, 1))[index], directIO.output)
            assertEquivalent(compiler.instructions, directIO.output)
        }
    }
}

package ru.tsaplev.app.execution.MatchCompilation

import ru.tsaplev.app.ASTreeNodes.BindingPaths
import ru.tsaplev.app.ASTreeNodes.MatchPattern
import ru.tsaplev.app.execution.DataValue
import ru.tsaplev.app.execution.stackMachine.MachineInstruction
import kotlin.math.max

class MatchPatternVisitor(val labelPrefix: String) {
    val instructions: MutableList<MachineInstruction> = mutableListOf()
    val bindingPaths = BindingPaths()
    var dropDepth = 0
    var maxDropDepth = 0

    fun visit(node: MatchPattern) {
        when(node) {
            is MatchPattern.Wirdcard -> {
                instructions.add(MachineInstruction.DROP())
            }
            is MatchPattern.Bind -> {
                bindingPaths.addBinding(node.name)
                visit(node.pattern)
            }
            is MatchPattern.Array -> {
                instructions.add(MachineInstruction.DUP())
                instructions.add(MachineInstruction.PATT("array"))
                instructions.add(MachineInstruction.JZ("${labelPrefix}_${dropDepth}"))

                instructions.add(MachineInstruction.DUP())
                instructions.add(MachineInstruction.LENGTH())
                instructions.add(MachineInstruction.CONST(node.patterns.size))
                instructions.add(MachineInstruction.BINOP("=="))
                instructions.add(MachineInstruction.JZ("${labelPrefix}_${dropDepth}"))

                dropDepth++
                node.patterns.indices.forEach {
                    instructions.add(MachineInstruction.DUP())
                    instructions.add(MachineInstruction.CONST(it))
                    instructions.add(MachineInstruction.ELEM())
                    bindingPaths.push(it)
                    visit(node.patterns[it])
                    bindingPaths.pop()
                }
                dropDepth--

                instructions.add(MachineInstruction.DROP())
            }
            is MatchPattern.SExpression -> {
                instructions.add(MachineInstruction.DUP())
                instructions.add(MachineInstruction.PATT("Tag ${node.name} ${node.patterns.size}"))
                instructions.add(MachineInstruction.JZ("${labelPrefix}_${dropDepth}"))
                dropDepth++
                maxDropDepth = max(dropDepth, maxDropDepth)
                node.patterns.indices.forEach {
                    instructions.add(MachineInstruction.DUP())
                    instructions.add(MachineInstruction.CONST(it))
                    instructions.add(MachineInstruction.ELEM())
                    bindingPaths.push(it)
                    visit(node.patterns[it])
                    bindingPaths.pop()
                }
                dropDepth--

                instructions.add(MachineInstruction.DROP())
            }
            is MatchPattern.Literal -> {
                when(node.expected) {
                    is DataValue.IntValue -> instructions.add(MachineInstruction.CONST(node.expected.value))
                    is DataValue.StringValue -> instructions.add(MachineInstruction.STRING(node.expected.value))
                    else -> throw IllegalStateException("Unexpected match pattern ${node.expected}")
                }
                instructions.add(MachineInstruction.BINOP("=="))
                instructions.add(MachineInstruction.JZ("${labelPrefix}_${dropDepth}"))
            }

            is MatchPattern.Type -> {
                instructions.add(MachineInstruction.PATT(node.name))
                instructions.add(MachineInstruction.JZ("${labelPrefix}_${dropDepth}"))
            }
        }
    }
}
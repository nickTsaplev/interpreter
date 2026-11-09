package ru.tsaplev.app.source

import ru.tsaplev.app.ASTNode
import ru.tsaplev.app.ASTreeNodes.*
import ru.tsaplev.app.execution.DataValue

class SourceParser(private val text: String) {
    private data class Token(val text: String, val offset: Int)

    private val tokens = tokenize()
    private var position = 0
    private val current: String
        get() = tokens[position].text

    fun parse(): ASTNode {
        expect("{")
        val program = statements("}")
        expect("}")
        if (current != "") fail("Unexpected token")
        return program
    }

    private fun tokenize(): List<Token> {
        val result = mutableListOf<Token>()
        var offset = 0
        while (offset < text.length) {
            if (text[offset].isWhitespace()) {
                offset++
                continue
            }
            if (text.startsWith("--", offset)) {
                offset = text.indexOf('\n', offset).let { if (it < 0) text.length else it }
                continue
            }
            if (text.startsWith("(*", offset)) {
                val end = text.indexOf("*)", offset + 2)
                if (end < 0) fail("Unclosed comment", offset)
                offset = end + 2
                continue
            }
            val token = tokenPattern.matchAt(text, offset)?.value ?: fail("Invalid token", offset)
            result.add(Token(token, offset))
            offset += token.length
        }
        result.add(Token("", text.length))
        return result
    }

    private fun fail(message: String, offset: Int = tokens[position].offset): Nothing {
        val line = text.take(offset).count { it == '\n' } + 1
        val column = offset - text.lastIndexOf('\n', offset - 1)
        throw IllegalArgumentException("$message at ($line:$column)")
    }

    private fun take(): String = tokens[position++].text

    private fun accept(token: String): Boolean {
        if (current != token) return false
        take()
        return true
    }

    private fun expect(token: String) {
        if (!accept(token)) fail("'$token' expected")
    }

    private fun identifier(): String {
        if (!isVariable(current)) fail("Identifier expected")
        return take()
    }

    private fun isVariable(token: String): Boolean =
        token.firstOrNull() in 'a'..'z' && token !in keywords

    private fun statements(vararg stops: String): ASTNode {
        val nodes = mutableListOf<ASTNode>()
        while (current !in stops && current != "") nodes.add(statement())
        if (nodes.isEmpty()) fail("Statement expected")
        return nodes.asReversed().reduce { right, left -> ASTSeq(left, right) }
    }

    private fun statement(): ASTNode = when {
        accept("{") -> statements("}").also { expect("}") }
        accept("read") -> ASTRead(parenthesized(::identifier)).also { accept(";") }
        accept("write") -> ASTFuncCall("write", parenthesized { expression() }).also { accept(";") }
        accept("skip") -> ASTSkip().also { accept(";") }
        accept("while") -> ASTWhile(parenthesized { expression() }, statement())
        accept("do") -> {
            val body = statement()
            expect("while")
            ASTDo(body, parenthesized { expression() }).also { accept(";") }
        }
        accept("for") -> parenthesized {
            val init = statement()
            val condition = expression()
            expect(";")
            val step = statement()
            Triple(init, condition, step)
        }.let { (init, condition, step) -> ASTSeq(init, ASTWhile(condition, ASTSeq(statement(), step))) }
        accept("if") -> conditional()
        accept("switch") -> switch()
        else -> assignment()
    }

    private fun conditional(): ASTNode {
        val condition = parenthesized { expression() }
        val thenBranch = statement()
        val elseBranch = when {
            accept("elif") -> conditional()
            accept("else") -> statement()
            else -> null
        }
        return ASTIf(condition, thenBranch, elseBranch)
    }

    private fun switch(): ASTNode {
        val value = parenthesized { expression() }
        expect("{")
        val branches = mutableListOf<Pair<MatchPattern, ASTNode>>()
        do {
            expect("case")
            val pattern = pattern()
            expect("->")
            branches.add(pattern to statements("|", "}"))
        } while (accept("|"))
        expect("}")
        accept(";")
        return ASTSwitch(value, branches)
    }

    private fun assignment(): ASTNode {
        val target = postfix(primary(), false)
        val operator = if (current in precedence) take() else null
        expect("=")
        val value = expression()
        accept(";")
        val source = if (operator == null) value else binary(operator, target, value)
        return when (target) {
            is ASTVar -> ASTAssign(target.name, source)
            is ASTElem -> ASTSet(target.elem, source, target.index)
            else -> fail("Invalid assignment target")
        }
    }

    private fun expression(minPriority: Int = 1): ASTNode {
        var left = postfix(primary())
        while (true) {
            val operator = current
            val priority = precedence[operator] ?: break
            if (priority < minPriority || tokens[position + 1].text == "=") break
            take()
            val right = expression(if (operator == ":") priority else priority + 1)
            left = binary(operator, left, right)
            if (priority == 3 && precedence[current] == 3) fail("Comparison cannot be chained")
        }
        return left
    }

    private fun binary(operator: String, left: ASTNode, right: ASTNode): ASTNode =
        if (operator == ":") ASTSExp("cons", listOf(left, right)) else ASTBinOP(operator, left, right)

    private fun postfix(base: ASTNode, allowLength: Boolean = true): ASTNode {
        var value = base
        while (true) {
            value = when {
                accept("[") -> ASTElem(value, expression().also { expect("]") })
                allowLength && accept(".") -> ASTLength(value).also { expect("length") }
                else -> return value
            }
        }
    }

    private fun primary(): ASTNode = when {
        accept("(") -> expression().also { expect(")") }
        accept("[") -> ASTArray(commaList("]", false) { expression() })
        accept("{") -> commaList("}") { expression() }.foldRight(ASTSExp("nil", emptyList())) { head, tail ->
            ASTSExp("cons", listOf(head, tail))
        }
        current.startsWith('"') -> ASTString(take().drop(1).dropLast(1))
        current.startsWith('\'') -> ASTConst(DataValue.IntValue(character()))
        current.firstOrNull() in '0'..'9' ->
            ASTConst(DataValue.IntValue(take().toIntOrNull() ?: fail("Integer out of range")))
        current.firstOrNull() in 'A'..'Z' -> {
            val name = take()
            ASTSExp(name, if (accept("(")) commaList(")", false) { expression() } else emptyList())
        }
        else -> ASTVar(identifier())
    }

    private fun pattern(): MatchPattern {
        val head = patternPrimary()
        return if (accept(":")) MatchPattern.constructor("cons", listOf(head, pattern())) else head
    }

    private fun patternPrimary(): MatchPattern = when {
        accept("_") -> MatchPattern.wildcard()
        accept("(") -> pattern().also { expect(")") }
        accept("[") -> MatchPattern.array(commaList("]", false, ::pattern))
        accept("{") -> commaList("}", true, ::pattern).foldRight(MatchPattern.constructor("nil", emptyList())) { head, tail ->
            MatchPattern.constructor("cons", listOf(head, tail))
        }
        accept("#") -> {
            if (current !in typePatterns) fail("Pattern type expected")
            MatchPattern.type(take()) ?: fail("Pattern type expected")
        }
        current.startsWith('"') -> MatchPattern.literal(DataValue.StringValue(take().drop(1).dropLast(1)))
        current.startsWith('\'') -> MatchPattern.literal(DataValue.IntValue(character()))
        current.firstOrNull() in 'A'..'Z' -> {
            val name = take()
            MatchPattern.constructor(name, if (accept("(")) commaList(")", false, ::pattern) else emptyList())
        }
        else -> {
            val name = identifier()
            val child = if (accept("@")) pattern() else MatchPattern.wildcard()
            MatchPattern.bind(name, child)
        }
    }

    private fun character(): Int {
        val value = take().drop(1).dropLast(1)
        return when (value) {
            "\\n" -> '\n'.code
            "\\r" -> '\r'.code
            "\\t" -> '\t'.code
            "\\0" -> 0
            else -> value.removePrefix("\\").singleOrNull()?.code ?: fail("Invalid character")
        }
    }

    private fun <T> parenthesized(parse: () -> T): T {
        expect("(")
        return parse().also { expect(")") }
    }

    private fun <T> commaList(end: String, allowEmpty: Boolean = true, parse: () -> T): List<T> {
        if (allowEmpty && accept(end)) return emptyList()
        val result = mutableListOf<T>()
        do {
            result.add(parse())
        } while (accept(","))
        expect(end)
        return result
    }

    companion object {
        private val tokenPattern = Regex("""[a-zA-Z][a-zA-Z_0-9']*|[0-9]+|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])'|->|!!|&&|==|!=|<=|>=|[{}()\[\],;.=_@#|:<>+*/%\-]""")
        private val keywords = setOf("read", "write", "while", "do", "for", "if", "else", "elif", "skip", "switch", "case")
        private val typePatterns = setOf("array", "int", "string", "sexp")
        private val precedence = mapOf(
            "!!" to 1, "&&" to 2, "==" to 3, "!=" to 3, "<=" to 3, "<" to 3, ">=" to 3, ">" to 3,
            ":" to 4, "+" to 5, "-" to 5, "*" to 6, "/" to 6, "%" to 6
        )
    }
}

package org.natalymr

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Assert.*
import org.junit.Test
import org.natalymr.interpreter.ImperativeLangLexer
import org.natalymr.interpreter.ImperativeLangParser
import java.io.ByteArrayInputStream

private fun eval(code: String, vararg i: Int): Int {
    val lexer = ImperativeLangLexer(CharStreams.fromString(code))
    val parser = ImperativeLangParser(CommonTokenStream(lexer))

    val codeArgs = i.joinToString(" ")
    val inputStream = ByteArrayInputStream(codeArgs.toByteArray(Charsets.UTF_8))

    return Interpreter(inputStream).visit(parser.program())
}

class InterpreterKtTest {
    @Test
    fun `id code returns its argument`() {
        val code =
            """
                read x;
                1: return x;
            """.trimIndent()

        assertEquals(10, eval(code, 10))
        assertEquals(5, eval(code, 5))
        assertEquals(-1, eval(code, -1))
    }

    @Test
    fun `heavy math`() {
        val code =
            """
                read x;
                1: return x + 10 - 20 + 30;
            """.trimIndent()
        val result1 = eval(
            code, 10
        )

        assertEquals(10 + 10 - 20 + 30, result1)

    }

    @Test
    fun `jump goto`() {
        val code =
            """
                read x;
                1: x := 2;
                   goto 2;
                2: return x;
            """.trimIndent()

        assertEquals(2, eval(code, 10))

    }

    @Test
    fun `jump if else EQ`() {
        val code =
            """
                read x;
                1: if x = 1 goto 2 else 3;
                2: return 2;
                3: return 3;
            """.trimIndent()

        assertEquals(2, eval(code, 1))
        assertEquals(3, eval(code, 10))
    }

    @Test
    fun `jump if else LT`() {
        val code =
            """
                read x;
                1: if x < 5 goto 2 else 3;
                2: return 2;
                3: return 3;
            """.trimIndent()

        assertEquals(2, eval(code, 1))
        assertEquals(3, eval(code, 7))
    }

    @Test
    fun `jump if else GT`() {
        val code =
            """
                read x;
                1: if x > 5 goto 2 else 3;
                2: return 2;
                3: return 3;
            """.trimIndent()

        assertEquals(2, eval(code, 7))
        assertEquals(3, eval(code, 1))
    }

    @Test
    fun `jump if else two expr`() {
        val code =
            """
                read x;
                1: if x - 2 + 1 = 1 + 13 - 2 goto 2 else 3;
                2: return 2;
                3: return 3;
            """.trimIndent()

        assertEquals(2, eval(code, 13))
        assertEquals(3, eval(code, 2))
    }

    @Test
    fun `several variables`() {
        val code =
            """
                read x, y, z;
                1: x := 2;
                   goto 2;
                2: return x + y + z;
            """.trimIndent()

        assertEquals(52, eval(code, 10, 20, 30))
    }

    @Test(expected = InterpreterException::class)
    fun `incorrect label`() {
        val code =
            """
                read x;
                1: x := 2;
                   goto 5;
                2: return x;
            """.trimIndent()

        eval(code, 1)
    }

    @Test(expected = InterpreterException::class)
    fun `incorrect variable`() {
        val code =
            """
                read x;
                1: y := 2;
                   goto 5;
                2: return x;
            """.trimIndent()

        eval(code, 1)
    }

    @Test
    fun Euclid() {
        val code =
            """
                read x, y;
                1: if x = y goto 7 else 2;
                2: if x < y goto 5 else 3;
                3: x := x - y;
                   goto 1;
                5: y := y - x;
                   goto 1;
                7: return x;
            """.trimIndent()

        assertEquals(6, eval(code, 12, 6))
        assertEquals(16, eval(code, 64, 48))
        assertEquals(3, eval(code, 111, 432))
        assertEquals(1, eval(code, 661, 113))
        assertEquals(21, eval(code, 1071, 462))
    }
}

package org.natalymr

import org.antlr.v4.runtime.*
import org.natalymr.interpreter.ImperativeLangLexer
import org.natalymr.interpreter.ImperativeLangParser
import org.antlr.v4.runtime.misc.ParseCancellationException

object ThrowingErrorListener : BaseErrorListener() {
    override fun syntaxError(
        recognizer: Recognizer<*, *>,
        offendingSymbol: Any,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException
    ) {
        throw ParseCancellationException("line $line:$charPositionInLine $msg")
    }
}

fun main() {
    val inputFile = "src/test/inputFileExamples/1.txt"
    val lexer = ImperativeLangLexer(CharStreams.fromFileName(inputFile)).also {
        it.removeErrorListeners()
        it.addErrorListener(ThrowingErrorListener)
    }

    val parser = ImperativeLangParser(CommonTokenStream(lexer)).also {
        it.removeErrorListeners()
        it.addErrorListener(ThrowingErrorListener)
    }

    println("Enter variables values:")
    println(Interpreter(System.`in`).visit(parser.program()))
}

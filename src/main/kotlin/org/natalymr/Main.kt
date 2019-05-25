package org.natalymr

import org.antlr.v4.runtime.*
import org.natalymr.interpreter.ImperativeLangLexer
import org.natalymr.interpreter.ImperativeLangParser
import org.antlr.v4.runtime.misc.ParseCancellationException
import kotlin.system.exitProcess

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

fun runCodeFromFile(inputFile: String) {
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

fun main(args: Array<String>) {

    if (args.isNotEmpty()) {
        runCodeFromFile(args[0])
    } else {
        println("Program file was not specified")
        exitProcess(-1)
    }
}

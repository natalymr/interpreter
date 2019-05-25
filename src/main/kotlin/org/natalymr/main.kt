package org.natalymr

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.natalymr.interpreter.ImperativeLangLexer
import org.natalymr.interpreter.ImperativeLangParser

fun main() {
    val inputFile = "src/test/inputFileExamples/1.txt"
    val lexer = ImperativeLangLexer(CharStreams.fromFileName(inputFile))
    val parser = ImperativeLangParser(CommonTokenStream(lexer))

    val a = Interpreter(System.`in`).visit(parser.program())
    println(a)
}
package org.natalymr

import org.antlr.v4.runtime.tree.TerminalNode
import org.natalymr.interpreter.ImperativeLangBaseVisitor
import org.natalymr.interpreter.ImperativeLangParser
import org.natalymr.interpreter.ImperativeLangParser.*
import java.io.InputStream
import java.util.*
import kotlin.collections.HashMap

private data class BasicBlockInfo(
    val label: LabelInfo,
    val assignment: List<AssignmentContext>,
    val jump: JumpContext
)

private data class LabelInfo(val value: String)

private fun LabelContext.toLabelInfo() = LabelInfo(text)

class Interpreter(private val inputStream: InputStream) : ImperativeLangBaseVisitor<Int>() {

    private val variableMap: HashMap<String, Int> = HashMap()
    private val labelMap: HashMap<LabelInfo, BasicBlockInfo> = HashMap()

    override fun visitProgram(ctx: ProgramContext): Int {
        val scanner = Scanner(inputStream)
        for (it in ctx.read().variable()) {
            variableMap[it.VARIABLE().text] = scanner.nextInt()
        }

        val basicBlocks = ctx.basicBlock().map { BasicBlockInfo(it.label().toLabelInfo(), it.assignment(), it.jump()) }
        basicBlocks.forEach {
            labelMap[it.label] = it
        }

        var currentBlock: BasicBlockInfo = basicBlocks.first()

        while (true) {
            currentBlock.assignment.forEach {
                visitAssignment(it)
            }

            if (currentBlock.jump.RETURN() != null) {
                return visitExpression(currentBlock.jump.expression().single())
            }

            currentBlock = myVisitJump(currentBlock.jump)
                ?: throw RuntimeException("No label ${currentBlock.jump.text} in current program!")
        }
    }

    override fun visitAssignment(ctx: AssignmentContext): Int {
        val newValue = this.visitExpression(ctx.expression())
        variableMap[ctx.variable().VARIABLE().text] = newValue

        return newValue
    }

    private fun myVisitJump(ctx: JumpContext): BasicBlockInfo? {
        // if
        if (ctx.IF() != null) {

            val success = if (ctx.right != null) {
                val leftExpr = visitExpression(ctx.left)
                val rightExpr = visitExpression(ctx.right)

                when (ctx.relop().text) {
                    "<" -> leftExpr < rightExpr
                    ">" -> leftExpr > rightExpr
                    "=" -> leftExpr == rightExpr
                    else -> throw RuntimeException("Unknown relational operator")
                }

            } else {
                0 != visitExpression(ctx.left)
            }

            if (success) {
                return labelMap[ctx.label()[0].toLabelInfo()]
            } else {
                return labelMap[ctx.label()[1].toLabelInfo()]
            }
        } else { // goto
            return labelMap[ctx.label().first().toLabelInfo()]
        }
    }

    override fun visitExpression(ctx: ExpressionContext): Int {
        if (ctx.signedVariable() != null) return visitSignedVariable(ctx.signedVariable())

        val leftExpression = visitExpression(ctx.left)
        val rightExpression = visitExpression(ctx.right)

        return if (ctx.PLUS() != null) {
            leftExpression + rightExpression
        } else {
            leftExpression - rightExpression
        }
    }

    override fun visitSignedVariable(ctx: SignedVariableContext): Int {

        if (ctx.PLUS() != null) {
            return visitSignedVariable(ctx.signedVariable())
        }
        if (ctx.MINUS() != null) {
            return -visitSignedVariable(ctx.signedVariable())
        }

        ctx.number()?.let { return it.text.toInt() }
        ctx.variable()?.let {
            return variableMap[it.text]
                ?: throw RuntimeException("No such variable, ${it.text}")
        }

        throw RuntimeException("incorrect visitSignedVariable")
    }

}
package org.natalymr

import org.natalymr.interpreter.ImperativeLangBaseVisitor
import org.natalymr.interpreter.ImperativeLangParser.*
import java.io.InputStream
import java.util.*

class InterpreterException(message: String) : Exception(message)

class Interpreter(private val inputStream: InputStream) : ImperativeLangBaseVisitor<Int>() {
    private val variableMap: MutableMap<String, Int> = mutableMapOf()
    private val labelMap: MutableMap<LabelInfo, BasicBlockInfo> = mutableMapOf()

    override fun visitProgram(ctx: ProgramContext): Int {
        val scanner = Scanner(inputStream)

        for (it in ctx.read().variable()) {
            variableMap[it.text] = scanner.nextInt()
        }

        val basicBlocks = ctx.basicBlock().map {
            BasicBlockInfo(it.label().toLabelInfo(), it.assignment(), it.jump())
        }

        basicBlocks.forEach {
            labelMap[it.label] = it
        }

        var currentBlock: BasicBlockInfo = basicBlocks.first()

        while (true) {
            currentBlock.assignment.forEach {
                visitAssignment(it)
            }

            if (currentBlock.jump.RETURN() != null) {
                return visitExpression(currentBlock.jump.returnedExpression)
            }

            currentBlock = getNextBasicBlock(currentBlock.jump)
                ?: throw InterpreterException("No label ${currentBlock.jump.text} in current program!")
        }
    }

    override fun visitAssignment(ctx: AssignmentContext): Int {
        val newValue = visitExpression(ctx.expression())
        variableMap[ctx.variable().text] = newValue

        return newValue
    }

    private fun getNextBasicBlock(ctx: JumpContext): BasicBlockInfo? {
        if (ctx.IF() == null) return labelMap[ctx.gotoLabel.toLabelInfo()]

        val success = if (ctx.right != null) {
            val leftExpr = visitExpression(ctx.left)
            val rightExpr = visitExpression(ctx.right)

            when (ctx.relop().text) {
                "<" -> leftExpr < rightExpr
                ">" -> leftExpr > rightExpr
                "=" -> leftExpr == rightExpr
                else -> throw InterpreterException("Unknown relational operator")
            }

        } else {
            0 != visitExpression(ctx.left)
        }

        val targetLabel = if (success) ctx.thenLabel else ctx.elseLabel
        return labelMap[targetLabel.toLabelInfo()]
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
                ?: throw InterpreterException("No variable ${it.text} present")
        }

        throw InterpreterException("Unsupported equation ${ctx.text}")
    }
}

private data class BasicBlockInfo(
    val label: LabelInfo,
    val assignment: List<AssignmentContext>,
    val jump: JumpContext
)

private data class LabelInfo(val value: String)

private fun LabelContext.toLabelInfo() = LabelInfo(text)

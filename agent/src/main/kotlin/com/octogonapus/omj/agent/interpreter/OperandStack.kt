package com.octogonapus.omj.agent.interpreter

import org.objectweb.asm.Opcodes.T_BYTE
import org.objectweb.asm.Opcodes.T_CHAR
import org.objectweb.asm.Opcodes.T_DOUBLE
import org.objectweb.asm.Opcodes.T_FLOAT
import org.objectweb.asm.Opcodes.T_INT
import org.objectweb.asm.Opcodes.T_LONG
import org.objectweb.asm.Opcodes.T_SHORT

@Suppress("DataClassPrivateConstructor")
internal data class OperandStack private constructor(
        private val stack: List<Operand>
) {

    internal fun applyOperation(operation: OperandStackOperation): OperandStack {
        val localStack = ArrayList(stack)
        localStack.applyOperation(operation)
        return OperandStack(localStack)
    }

    private fun MutableList<Operand>.applyOperation(op: OperandStackOperation) {
        when (op) {
            OperandStackOperation.NOP -> nop()
            is OperandStackOperation.PushConstInt -> push(Operand.IntType.ConstInt(op.value))
            is OperandStackOperation.PushConstLong -> push(Operand.LongType.ConstLong(op.value))
            is OperandStackOperation.PushConstFloat -> push(Operand.FloatType.ConstFloat(op.value))
            is OperandStackOperation.PushConstDouble -> push(Operand.DoubleType.ConstDouble(op.value))
            is OperandStackOperation.PushConstByte -> push(Operand.ByteType.ConstByte(op.value))
            is OperandStackOperation.PushConstShort -> push(Operand.ShortType.ConstShort(op.value))
            is OperandStackOperation.NewArray -> {
                check(pop() is Operand.IntType)
                push(Operand.RefType.ArrayRef(op.type))
            }
            OperandStackOperation.PushNullRef -> push(Operand.RefType.Null)
            OperandStackOperation.LoadIntFromArray -> {
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_INT))
                push(Operand.IntType.RuntimeInt)
            }
            OperandStackOperation.StoreIntoIntArray -> {
                check(pop() is Operand.IntType)
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_INT))
            }
            OperandStackOperation.LoadLongFromArray -> {
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_LONG))
                push(Operand.LongType.RuntimeLong)
            }
            OperandStackOperation.StoreIntoLongArray -> {
                check(pop() is Operand.LongType)
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_LONG))
            }
            OperandStackOperation.LoadFloatFromArray -> {
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_FLOAT))
                push(Operand.FloatType.RuntimeFloat)
            }
            OperandStackOperation.StoreIntoFloatArray -> {
                check(pop() is Operand.FloatType)
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_FLOAT))
            }
            OperandStackOperation.LoadDoubleFromArray -> {
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_DOUBLE))
                push(Operand.DoubleType.RuntimeDouble)
            }
            OperandStackOperation.StoreIntoDoubleArray -> {
                check(pop() is Operand.DoubleType)
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_DOUBLE))
            }
            OperandStackOperation.LoadRefFromArray -> {
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.RefArrayRef)
                push(Operand.RefType.RuntimeRef)
            }
            OperandStackOperation.StoreIntoRefArray -> {
                check(pop() is Operand.RefType)
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.RefArrayRef)
            }
            OperandStackOperation.LoadByteFromArray -> {
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_BYTE))
                push(Operand.ByteType.RuntimeByte)
            }
            OperandStackOperation.StoreIntoByteArray -> {
                check(pop() is Operand.ByteType)
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_BYTE))
            }
            OperandStackOperation.LoadCharFromArray -> {
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_CHAR))
                push(Operand.CharType.RuntimeChar)
            }
            OperandStackOperation.StoreIntoCharArray -> {
                check(pop() is Operand.CharType)
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_CHAR))
            }
            OperandStackOperation.LoadShortFromArray -> {
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_SHORT))
                push(Operand.ShortType.RuntimeShort)
            }
            OperandStackOperation.StoreIntoShortArray -> {
                check(pop() is Operand.ShortType)
                check(pop() is Operand.IntType)
                check(pop() == Operand.RefType.ArrayRef(T_SHORT))
            }
        }
    }

    companion object {

        internal fun from(vararg operands: Operand): OperandStack {
            val stack = operands.toList()
            return OperandStack(stack)
        }

        private fun nop() = Unit

        private fun MutableList<Operand>.push(operand: Operand) {
            add(operand)
        }

        @OptIn(ExperimentalStdlibApi::class)
        private fun MutableList<Operand>.pop(): Operand {
            return removeLast()
        }
    }
}

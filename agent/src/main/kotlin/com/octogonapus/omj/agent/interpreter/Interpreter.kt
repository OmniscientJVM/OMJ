/*
 * This file is part of OMJ.
 *
 * OMJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OMJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OMJ.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.octogonapus.omj.agent.interpreter

import org.objectweb.asm.Opcodes.AALOAD
import org.objectweb.asm.Opcodes.AASTORE
import org.objectweb.asm.Opcodes.ACONST_NULL
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.BALOAD
import org.objectweb.asm.Opcodes.BASTORE
import org.objectweb.asm.Opcodes.BIPUSH
import org.objectweb.asm.Opcodes.CALOAD
import org.objectweb.asm.Opcodes.CASTORE
import org.objectweb.asm.Opcodes.DALOAD
import org.objectweb.asm.Opcodes.DASTORE
import org.objectweb.asm.Opcodes.DCONST_0
import org.objectweb.asm.Opcodes.DCONST_1
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.DUP2
import org.objectweb.asm.Opcodes.DUP2_X1
import org.objectweb.asm.Opcodes.DUP2_X2
import org.objectweb.asm.Opcodes.DUP_X1
import org.objectweb.asm.Opcodes.DUP_X2
import org.objectweb.asm.Opcodes.FALOAD
import org.objectweb.asm.Opcodes.FASTORE
import org.objectweb.asm.Opcodes.FCONST_0
import org.objectweb.asm.Opcodes.FCONST_2
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.IALOAD
import org.objectweb.asm.Opcodes.IASTORE
import org.objectweb.asm.Opcodes.ICONST_5
import org.objectweb.asm.Opcodes.ICONST_M1
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LALOAD
import org.objectweb.asm.Opcodes.LASTORE
import org.objectweb.asm.Opcodes.LCONST_0
import org.objectweb.asm.Opcodes.LCONST_1
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.Opcodes.NEWARRAY
import org.objectweb.asm.Opcodes.NOP
import org.objectweb.asm.Opcodes.POP
import org.objectweb.asm.Opcodes.POP2
import org.objectweb.asm.Opcodes.SALOAD
import org.objectweb.asm.Opcodes.SASTORE
import org.objectweb.asm.Opcodes.SIPUSH
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * This class is NOT thread-safe.
 */
internal class Interpreter {

    private val stackMap: MutableMap<AbstractInsnNode, OperandStack> = HashMap()

    /**
     * Determine the state of the stack immediately before the [insn] executes.
     */
    internal fun stackBefore(insn: AbstractInsnNode) = stackAfter(insn.previous)

    /**
     * Determine the state of the stack immediately after the [insn] executes.
     */
    internal fun stackAfter(insn: AbstractInsnNode): OperandStack {
        return stackMap.getOrPut(insn) {
            computeStack(insn)
        }
    }

    /**
     * Changes every occurrence of the [old] operand to the [new] operand.
     *
     * @param old The old operand to get rid of.
     * @param new The new operand to replace the [old] with.
     */
    internal fun updateOperand(old: Operand, new: Operand) {
        val newMap = stackMap.mapValues { (_, stack) ->
            stack.updateOperand(old, new)
        }

        stackMap.clear()
        stackMap.putAll(newMap)
    }

    /**
     * Sets the stack for the first insn. FOR USE IN TESTS ONLY!
     */
    internal fun setStackAfter(insn: AbstractInsnNode, stack: OperandStack) {
        stackMap[insn] = stack
    }

    private fun computeStack(insn: AbstractInsnNode): OperandStack {
        val prevInsn = insn.previous
        val prevStack = if (prevInsn == null) {
            // insn is the first insn in the list
            OperandStack.from()
        } else stackAfter(prevInsn)

        return interpret(insn, prevStack)
    }

    private fun interpret(insn: AbstractInsnNode, stack: OperandStack): OperandStack {
        val operation = stackOperationFor(insn)
        return stack.applyOperation(operation)
    }

    @Suppress("ThrowsCount", "ComplexMethod")
    private fun stackOperationFor(insn: AbstractInsnNode): OperandStackOperation {
        return when (insn) {
            is InsnNode -> when (insn.opcode) {
                NOP -> OperandStackOperation.NOP
                ACONST_NULL -> OperandStackOperation.PushNullRef
                in ICONST_M1..ICONST_5 ->
                    OperandStackOperation.PushConstInt((insn.opcode - ICONST_M1) - 1)
                in LCONST_0..LCONST_1 ->
                    OperandStackOperation.PushConstLong(insn.opcode - LCONST_0.toLong())
                in FCONST_0..FCONST_2 ->
                    OperandStackOperation.PushConstFloat(insn.opcode - FCONST_0.toFloat())
                in DCONST_0..DCONST_1 ->
                    OperandStackOperation.PushConstDouble(insn.opcode - DCONST_0.toDouble())
                IALOAD -> OperandStackOperation.LoadIntFromArray
                IASTORE -> OperandStackOperation.StoreIntoIntArray
                LALOAD -> OperandStackOperation.LoadLongFromArray
                LASTORE -> OperandStackOperation.StoreIntoLongArray
                FALOAD -> OperandStackOperation.LoadFloatFromArray
                FASTORE -> OperandStackOperation.StoreIntoFloatArray
                DALOAD -> OperandStackOperation.LoadDoubleFromArray
                DASTORE -> OperandStackOperation.StoreIntoDoubleArray
                AALOAD -> OperandStackOperation.LoadRefFromArray
                AASTORE -> OperandStackOperation.StoreIntoRefArray
                BALOAD -> OperandStackOperation.LoadByteFromArray
                BASTORE -> OperandStackOperation.StoreIntoByteArray
                CALOAD -> OperandStackOperation.LoadCharFromArray
                CASTORE -> OperandStackOperation.StoreIntoCharArray
                SALOAD -> OperandStackOperation.LoadShortFromArray
                SASTORE -> OperandStackOperation.StoreIntoShortArray
                POP -> OperandStackOperation.Pop
                POP2 -> OperandStackOperation.Pop2
                DUP -> OperandStackOperation.Dup
                DUP_X1 -> OperandStackOperation.DupX1
                DUP_X2 -> OperandStackOperation.DupX2
                DUP2 -> OperandStackOperation.Dup2
                DUP2_X1 -> OperandStackOperation.Dup2X1
                DUP2_X2 -> OperandStackOperation.Dup2X2
                else -> throw UnsupportedOperationException("Unknown insn: $insn")
            }

            is IntInsnNode -> when (insn.opcode) {
                BIPUSH -> OperandStackOperation.PushConstByte(insn.operand)
                SIPUSH -> OperandStackOperation.PushConstShort(insn.operand)
                NEWARRAY -> OperandStackOperation.NewArray(insn.operand)
                else -> throw UnsupportedOperationException("Unknown insn: $insn")
            }

            is VarInsnNode -> when (insn.opcode) {
                ILOAD -> OperandStackOperation.LoadIntFromLocal(insn.`var`)
                LLOAD -> OperandStackOperation.LoadLongFromLocal(insn.`var`)
                FLOAD -> OperandStackOperation.LoadFloatFromLocal(insn.`var`)
                DLOAD -> OperandStackOperation.LoadDoubleFromLocal(insn.`var`)
                ALOAD -> OperandStackOperation.LoadRefFromLocal(insn.`var`)
                ISTORE -> OperandStackOperation.StoreIntIntoLocal(insn.`var`)
                LSTORE -> OperandStackOperation.StoreLongIntoLocal(insn.`var`)
                FSTORE -> OperandStackOperation.StoreFloatIntoLocal(insn.`var`)
                DSTORE -> OperandStackOperation.StoreDoubleIntoLocal(insn.`var`)
                ASTORE -> OperandStackOperation.StoreRefIntoLocal(insn.`var`)
                else -> throw UnsupportedOperationException("Unknown insn: $insn")
            }

            else -> throw UnsupportedOperationException("Unknown insn: $insn")
        }
    }
}

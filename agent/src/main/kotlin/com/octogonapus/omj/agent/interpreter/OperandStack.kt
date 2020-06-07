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
            is OperandStackOperation.PushConstDouble ->
                push(Operand.DoubleType.ConstDouble(op.value))
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
            OperandStackOperation.Pop -> {
                check(pop().category() == Category.CategoryOne)
            }
            OperandStackOperation.Pop2 -> {
                when (pop().category()) {
                    Category.CategoryOne -> check(pop().category() == Category.CategoryOne)
                    Category.CategoryTwo -> nop()
                }
            }
            OperandStackOperation.Dup -> {
                val value = peek()
                check(value.category() == Category.CategoryOne)
                push(value)
            }
            OperandStackOperation.DupX1 -> {
                val value1 = peek()
                check(value1.category() == Category.CategoryOne)
                val value2 = peek(1)
                check(value2.category() == Category.CategoryOne)
                push(value1, 1)
            }
            OperandStackOperation.DupX2 -> {
                val value1 = peek()
                check(value1.category() == Category.CategoryOne)
                val value2 = peek(1)
                when (value2.category()) {
                    Category.CategoryOne -> {
                        val value3 = peek(2)
                        check(value3.category() == Category.CategoryOne)
                        push(value1, 2)
                    }

                    Category.CategoryTwo -> push(value1, 1)
                }
            }
            OperandStackOperation.Dup2 -> {
                val value1 = peek()
                when (value1.category()) {
                    Category.CategoryOne -> {
                        val value2 = peek(1)
                        check(value2.category() == Category.CategoryOne)
                        push(value2)
                        push(value1)
                    }

                    Category.CategoryTwo -> push(value1)
                }
            }
            OperandStackOperation.Dup2X1 -> {
                val value1 = peek()
                when (value1.category()) {
                    Category.CategoryOne -> {
                        val value2 = peek(1)
                        check(value2.category() == Category.CategoryOne)
                        val value3 = peek(2)
                        check(value3.category() == Category.CategoryOne)
                        push(value2, 2)
                        push(value1, 2)
                    }

                    Category.CategoryTwo -> {
                        val value2 = peek(1)
                        check(value2.category() == Category.CategoryOne)
                        push(value1, 1)
                    }
                }
            }
            OperandStackOperation.Dup2X2 -> {
                val value1 = peek()
                when (value1.category()) {
                    Category.CategoryOne -> {
                        val value2 = peek(1)
                        check(value2.category() == Category.CategoryOne)
                        val value3 = peek(2)
                        when (value3.category()) {
                            Category.CategoryOne -> {
                                val value4 = peek(3)
                                check(value4.category() == Category.CategoryOne)
                                push(value2, 3)
                                push(value1, 3)
                            }

                            Category.CategoryTwo -> {
                                push(value2, 2)
                                push(value1, 2)
                            }
                        }
                    }

                    Category.CategoryTwo -> {
                        val value2 = peek(1)
                        when (value2.category()) {
                            Category.CategoryOne -> {
                                val value3 = peek(2)
                                check(value3.category() == Category.CategoryOne)
                                push(value1, 2)
                            }

                            Category.CategoryTwo -> push(value1, 1)
                        }
                    }
                }
            }
        }
    }

    companion object {

        /**
         * Creates the operand stack from the given operands. The stack grows from left to right.
         * For example:
         *   from(op1, op2)
         *   becomes
         *   op2
         *   op1
         */
        internal fun from(vararg operands: Operand): OperandStack {
            val stack = operands.toList()
            return OperandStack(stack)
        }

        private fun nop() = Unit

        private fun MutableList<Operand>.push(operand: Operand) {
            add(operand)
        }

        private fun MutableList<Operand>.push(operand: Operand, offset: Int) =
            add(size - 1 - offset, operand)

        @OptIn(ExperimentalStdlibApi::class)
        private fun MutableList<Operand>.pop() = removeLast()

        private fun List<Operand>.peek() = peek(0)

        private fun List<Operand>.peek(offset: Int) = this[lastIndex - offset]
    }
}

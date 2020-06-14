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

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.checkAll
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.TypeInsnNode

internal class TypeInsnTest : StringSpec({
    "new object" {
        singleInsnTest(
            insn = TypeInsnNode(Opcodes.NEW, "Ljava/lang/Object;"),
            stackAfter = OperandStack.from(Operand.RefType.RuntimeRef("Ljava/lang/Object;"))
        )
    }

    "anewarray" {
        checkAll(nonNegInts) { index ->
            singleInsnTest(
                insn = TypeInsnNode(Opcodes.ANEWARRAY, "Ljava/lang/Object;"),
                stackBefore = OperandStack.from(index),
                stackAfter = OperandStack.from(
                    Operand.RefType.ArrayRef(ArrayType.Ref("Ljava/lang/Object;"))
                )
            )
        }
    }

    "checkcast" {
        singleInsnTest(
            insn = TypeInsnNode(Opcodes.CHECKCAST, "Ljava/lang/Object;"),
            stackBefore = OperandStack.from(Operand.RefType.RuntimeRef(null)),
            stackAfter = OperandStack.from(Operand.RefType.RuntimeRef(null))
        )
    }

    "instanceof" {
        singleInsnTest(
            insn = TypeInsnNode(Opcodes.INSTANCEOF, "Ljava/lang/Object;"),
            stackBefore = OperandStack.from(Operand.RefType.RuntimeRef(null)),
            stackAfter = OperandStack.from(Operand.IntType.RuntimeInt())
        )
    }
})

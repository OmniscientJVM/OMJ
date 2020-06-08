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
import org.objectweb.asm.tree.InsnNode

class OtherZeroOperandInsns : StringSpec({
    "nop" {
        singleInsnTest(
            insn = InsnNode(Opcodes.NOP),
            stackAfter = OperandStack.from()
        )
    }

    "aconst_null" {
        singleInsnTest(
            insn = InsnNode(Opcodes.ACONST_NULL),
            stackAfter = OperandStack.from(Operand.RefType.Null())
        )
    }

    "swap" {
        checkAll(allCategory1Values, allCategory1Values) { value1, value2 ->
            singleInsnTest(
                insn = InsnNode(Opcodes.SWAP),
                stackBefore = OperandStack.from(value2, value1),
                stackAfter = OperandStack.from(value1, value2)
            )
        }
    }
})

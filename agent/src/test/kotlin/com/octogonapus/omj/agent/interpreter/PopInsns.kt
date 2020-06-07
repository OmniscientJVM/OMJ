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
import java.lang.RuntimeException
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.InsnNode

class PopInsns : StringSpec({
    "pop a category 1 value" {
        checkAll(allCategory1Values) { value ->
            singleInsnTest(
                insn = InsnNode(Opcodes.POP),
                stackBefore = OperandStack.from(value),
                stackAfter = OperandStack.from()
            )
        }
    }

    "pop a category 2 value" {
        checkAll(allCategory2Values) { value ->
            singleInsnShouldThrowTest<IllegalStateException>(
                insn = InsnNode(Opcodes.POP),
                stackBefore = OperandStack.from(value)
            )
        }
    }

    "pop2 a category 1 value" {
        checkAll(allCategory1Values) { value ->
            singleInsnShouldThrowTest<RuntimeException>(
                insn = InsnNode(Opcodes.POP2),
                stackBefore = OperandStack.from(value)
            )
        }
    }

    "pop2 two category 1 value" {
        checkAll(allCategory1Values) { value ->
            singleInsnTest(
                insn = InsnNode(Opcodes.POP2),
                stackBefore = OperandStack.from(value, value),
                stackAfter = OperandStack.from()
            )
        }
    }

    "pop2 a category 2 value" {
        checkAll(allCategory2Values) { value ->
            singleInsnTest(
                insn = InsnNode(Opcodes.POP2),
                stackBefore = OperandStack.from(value),
                stackAfter = OperandStack.from()
            )
        }
    }
})

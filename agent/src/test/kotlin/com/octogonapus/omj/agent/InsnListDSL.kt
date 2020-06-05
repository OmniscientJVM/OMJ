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
package com.octogonapus.omj.agent

import com.octogonapus.omj.testutil.shouldHaveInOrder
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.VarInsnNode

@DslMarker
annotation class InsnListDSL

@InsnListDSL
class CheckInsns(private val insnList: InsnList) {

    private val checks = mutableListOf<(AbstractInsnNode) -> Boolean>()

    @InsnListDSL
    fun lineNumber(lineNumber: Int) {
        checks.add {
            it is LineNumberNode && it.line == lineNumber
        }
    }

    @InsnListDSL
    fun insn(opcode: Int) {
        checks.add {
            it is InsnNode && it.opcode == opcode
        }
    }

    @InsnListDSL
    fun variable(opcode: Int, index: Int) {
        checks.add {
            it is VarInsnNode && it.opcode == opcode && it.`var` == index
        }
    }

    @InsnListDSL
    fun ldc(cst: Any) {
        checks.add {
            it is LdcInsnNode && it.cst == cst
        }
    }

    @InsnListDSL
    fun method(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
        checks.add {
            it is MethodInsnNode &&
                it.opcode == opcode &&
                it.owner == owner &&
                it.name == name &&
                it.desc == descriptor &&
                it.itf == isInterface
        }
    }

    fun check() = insnList.toList().shouldHaveInOrder(checks)
}

@InsnListDSL
fun checkInsns(insnList: InsnList, configure: CheckInsns.() -> Unit) {
    val checkInsns = CheckInsns(insnList)
    checkInsns.configure()
    checkInsns.check()
}

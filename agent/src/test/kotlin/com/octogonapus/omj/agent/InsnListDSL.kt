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

import com.octogonapus.omj.testutil.shouldHaveExactlyInOrder
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MultiANewArrayInsnNode
import org.objectweb.asm.tree.TypeInsnNode
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
    fun iinc(index: Int) {
        checks.add {
            it is IincInsnNode && it.`var` == index
        }
    }

    @InsnListDSL
    fun insn(opcode: Int) {
        checks.add {
            it is InsnNode && it.opcode == opcode
        }
    }

    @InsnListDSL
    fun varInsn(opcode: Int, index: Int) {
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

    @InsnListDSL
    fun type(opcode: Int, descriptor: String) {
        checks.add {
            it is TypeInsnNode && it.opcode == opcode && it.desc == descriptor
        }
    }

    @InsnListDSL
    fun field(opcode: Int, fieldOwner: String, fieldName: String, fieldDesc: String) {
        checks.add {
            it is FieldInsnNode &&
                it.opcode == opcode &&
                it.owner == fieldOwner &&
                it.name == fieldName &&
                it.desc == fieldDesc
        }
    }

    @InsnListDSL
    fun intInsn(opcode: Int, operand: Int) {
        checks.add {
            it is IntInsnNode && it.opcode == opcode && it.operand == operand
        }
    }

    @InsnListDSL
    fun typeInsn(opcode: Int, desc: String) {
        checks.add {
            it is TypeInsnNode && it.opcode == opcode && it.desc == desc
        }
    }

    @InsnListDSL
    fun multiANewArrayInsn(desc: String, dims: Int) {
        checks.add {
            it is MultiANewArrayInsnNode && it.desc == desc && it.dims == dims
        }
    }

    @InsnListDSL
    fun jumpInsn(opcode: Int) {
        checks.add {
            it is JumpInsnNode && it.opcode == opcode
        }
    }

    @InsnListDSL
    fun label() {
        checks.add {
            it is LabelNode
        }
    }

    fun check() = insnList.toList().shouldHaveExactlyInOrder(checks)
}

@InsnListDSL
fun checkInsns(insnList: InsnList, configure: CheckInsns.() -> Unit) {
    val checkInsns = CheckInsns(insnList)
    checkInsns.configure()
    checkInsns.check()
}

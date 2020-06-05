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

import com.octogonapus.omj.agent.MethodAdapterUtil.Companion.agentLibClassName
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes.ASM8
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode

internal class OMJClassTransformerTest {

    @Test
    fun fdshdsjfhskd() {
        val methodNode = MethodNode(
            ASM8,
            0,
            "methodName",
            "()V",
            null,
            null
        ).apply {
            localVariables.add(LocalVariableNode("varName", "I", null, LabelNode(), LabelNode(), 1))
            instructions.apply {
                add(LineNumberNode(10, LabelNode()))
                add(VarInsnNode(ISTORE, 1))
            }
        }

        val classNode = ClassNode(ASM8).apply {
            name = "className"
            superName = "superClassName"
            methods.add(methodNode)
        }

        val transformer = OMJClassTransformer(classNode)
        transformer.transform()

        methodNode.instructions.toList().shouldContainExactly(
            LineNumberNode(10, LabelNode()),
            InsnNode(DUP),
            VarInsnNode(ISTORE, 1),
            LdcInsnNode("className"),
            LdcInsnNode(10),
            LdcInsnNode("varName"),
            MethodInsnNode(
                INVOKESTATIC,
                agentLibClassName,
                "store",
                "(ILjava/lang/String;ILjava/lang/String;",
                false
            )
        )
    }
}

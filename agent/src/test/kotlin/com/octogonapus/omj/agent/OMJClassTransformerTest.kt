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
import com.octogonapus.omj.testutil.KoinTestFixture
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.dsl.module
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ASM8
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode

internal class OMJClassTransformerTest : KoinTestFixture() {

    @Nested
    inner class MethodCalls {

        @Test
        fun `visit method insn with an owner class that will be transformed`() {
            testKoin(
                module {
                    single {
                        mockk<ClassFilter> {
                            // We will transform the method owner, so the preamble should be emitted
                            every { shouldTransform(methodOwner) } returns true
                        }
                    }
                }
            )

            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(
                        MethodInsnNode(
                            INVOKEVIRTUAL,
                            methodOwner,
                            methodName,
                            "()V",
                            false
                        )
                    )
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Load the preamble
                ldc(className)
                method(INVOKESTATIC, agentLibClassName, "className", "(Ljava/lang/String;)V", false)
                ldc(lineNumber)
                method(INVOKESTATIC, agentLibClassName, "lineNumber", "(I)V", false)
                ldc(methodName)
                method(
                    INVOKESTATIC,
                    agentLibClassName,
                    "methodName",
                    "(Ljava/lang/String;)V",
                    false
                )

                // Emit the method call
                method(INVOKEVIRTUAL, methodOwner, methodName, "()V", false)
            }
        }

        @Test
        fun `visit method insn with an owner class that will not be transformed`() {
            testKoin(
                module {
                    single {
                        mockk<ClassFilter> {
                            // We will not transform the method owner, so the preamble should not be
                            // emitted
                            every { shouldTransform(methodOwner) } returns false
                        }
                    }
                }
            )

            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(
                        MethodInsnNode(
                            INVOKEVIRTUAL,
                            methodOwner,
                            methodName,
                            "()V",
                            false
                        )
                    )
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // No preamble because we will not transform the method owner

                // Emit the method call
                method(INVOKEVIRTUAL, methodOwner, methodName, "()V", false)
            }
        }
    }

    @Nested
    inner class LocalVariableTracing {

        @ParameterizedTest
        @ValueSource(ints = [ISTORE, LSTORE, FSTORE, DSTORE, ASTORE])
        fun `all store insns for a local variable`(opcode: Int) {
            val localVariableDesc = OpcodeUtil.getLoadStoreDescriptor(opcode)
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(makeLocalVariable(varName, localVariableDesc, 1)),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(VarInsnNode(opcode, 1))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Dup what's on the stack that will be stored
                insn(OpcodeUtil.getDupOpcode(opcode))

                local(opcode, 1)

                // Load the context and trace the store
                ldc(className)
                ldc(lineNumber)
                ldc(varName)
                method(
                    INVOKESTATIC,
                    agentLibClassName,
                    "store",
                    "(${localVariableDesc}Ljava/lang/String;ILjava/lang/String;)V",
                    false
                )
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [ILOAD, LLOAD, FLOAD, DLOAD, ALOAD])
        fun `all load insns for a local variable`(opcode: Int) {
            val localVariableDesc = OpcodeUtil.getLoadStoreDescriptor(opcode)
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(makeLocalVariable(varName, localVariableDesc, 1)),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(VarInsnNode(opcode, 1))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            // No instrumentation for loads
            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)
                local(opcode, 1)
            }
        }

        @Test
        fun `iinc insn`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(makeLocalVariable(varName, "I", 1)),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(IincInsnNode(1, 2)) // Increment doesn't matter
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)
                iinc(1)

                // Load the value of the local after the increment
                local(ILOAD, 1)

                // Load the context and trace the store
                ldc(className)
                ldc(lineNumber)
                ldc(varName)
                method(
                    INVOKESTATIC,
                    agentLibClassName,
                    "store",
                    "(ILjava/lang/String;ILjava/lang/String;)V",
                    false
                )
            }
        }
    }

    private fun makeClassNode(name: String, superName: String, method: MethodNode) =
        ClassNode(ASM8).also {
            it.name = name
            it.superName = superName
            it.methods.add(method)
        }

    private fun makeMethodNode(
        access: Int,
        name: String,
        desc: String,
        localVariables: List<LocalVariableNode>,
        insnList: InsnList
    ) = MethodNode(ASM8, access, name, desc, null, null).also {
        it.localVariables = localVariables
        it.instructions = insnList
    }

    private fun makeLocalVariable(name: String, desc: String, index: Int) =
        LocalVariableNode(name, desc, null, LabelNode(), LabelNode(), index)

    companion object {
        private const val className = "className"
        private const val superClassName = "superClassName"
        private const val methodName = "methodName"
        private const val methodOwner = "methodOwner"
        private const val varName = "varName"
        private const val lineNumber = 3495
    }
}

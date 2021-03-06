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

import com.octogonapus.omj.agent.OMJClassTransformer.Companion.agentLibClassName
import com.octogonapus.omj.testutil.KoinTestFixture
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.dsl.module
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.AALOAD
import org.objectweb.asm.Opcodes.AASTORE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ANEWARRAY
import org.objectweb.asm.Opcodes.ASM8
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.BASTORE
import org.objectweb.asm.Opcodes.BIPUSH
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.DUP2
import org.objectweb.asm.Opcodes.DUP2_X1
import org.objectweb.asm.Opcodes.DUP_X1
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.IASTORE
import org.objectweb.asm.Opcodes.ICONST_0
import org.objectweb.asm.Opcodes.ICONST_1
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.Opcodes.NEW
import org.objectweb.asm.Opcodes.NEWARRAY
import org.objectweb.asm.Opcodes.PUTFIELD
import org.objectweb.asm.Opcodes.PUTSTATIC
import org.objectweb.asm.Opcodes.T_BOOLEAN
import org.objectweb.asm.Opcodes.T_INT
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.MultiANewArrayInsnNode
import org.objectweb.asm.tree.TypeInsnNode
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

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Load the preamble
                methodPreamble(className, lineNumber, methodName)

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

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
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
    inner class MethodBodies {

        @Test
        fun `visit virtual method with no args`() {
            testKoin(
                module {
                    single {
                        mockk<DynamicClassDefiner> {
                            every { defineClassForMethod("()V", false) } returns dynamicClassName
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
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            checkInsns(methodNode.instructions) {
                startMethodTrace(dynamicClassName)
                receiver()
                endMethodTrace()

                lineNumber(lineNumber)
            }
        }

        @Test
        fun `visit static method with no args`() {
            testKoin(
                module {
                    single {
                        mockk<DynamicClassDefiner> {
                            every { defineClassForMethod("()V", true) } returns dynamicClassName
                        }
                    }
                }
            )

            val methodNode = makeMethodNode(
                ACC_STATIC,
                methodName,
                "()V",
                listOf(),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            checkInsns(methodNode.instructions) {
                startMethodTrace(dynamicClassName)
                endMethodTrace()

                lineNumber(lineNumber)
            }
        }

        @Test
        fun `visit virtual method with one int arg`() {
            testKoin(
                module {
                    single {
                        mockk<DynamicClassDefiner> {
                            every { defineClassForMethod("(I)V", false) } returns dynamicClassName
                        }
                    }
                }
            )

            val methodNode = makeMethodNode(
                0,
                methodName,
                "(I)V",
                listOf(makeLocalVariable("myInt", "I", 1)),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            checkInsns(methodNode.instructions) {
                startMethodTrace(dynamicClassName)
                receiver()
                varInsn(ILOAD, 1)
                method(INVOKESTATIC, agentLibClassName, "methodCall_argument_int", "(I)V", false)
                endMethodTrace()

                lineNumber(lineNumber)
            }
        }

        @Test
        fun `visit static method with one double arg`() {
            testKoin(
                module {
                    single {
                        mockk<DynamicClassDefiner> {
                            every { defineClassForMethod("(D)V", true) } returns dynamicClassName
                        }
                    }
                }
            )

            val methodNode = makeMethodNode(
                ACC_STATIC,
                methodName,
                "(D)V",
                listOf(makeLocalVariable("myDouble", "D", 0)),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            checkInsns(methodNode.instructions) {
                startMethodTrace(dynamicClassName)
                varInsn(DLOAD, 0)
                method(INVOKESTATIC, agentLibClassName, "methodCall_argument_double", "(D)V", false)
                endMethodTrace()

                lineNumber(lineNumber)
            }
        }

        @Test
        fun `visit main method`() {
            testKoin(
                module {
                    single {
                        mockk<DynamicClassDefiner> {
                            every { defineClassForMethod("([Ljava/lang/String;)V", true) } returns
                                dynamicClassName
                        }
                    }
                }
            )

            val methodNode = makeMethodNode(
                ACC_PUBLIC + ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                listOf(makeLocalVariable("args", "[Ljava/lang/String;", 0)),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            checkInsns(methodNode.instructions) {
                // Emit a preamble right before recording the trace because the main method is the
                // entry point, so it might not have a preamble otherwise.
                methodPreamble(className, lineNumber, "main")

                startMethodTrace(dynamicClassName)
                varInsn(ALOAD, 0)
                method(
                    INVOKESTATIC,
                    agentLibClassName,
                    "methodCall_argument_Object",
                    "(Ljava/lang/Object;)V",
                    false
                )
                endMethodTrace()

                lineNumber(lineNumber)
            }
        }

        @Test
        fun `visit instance initialization method`() {
            testKoin(
                module {
                    single {
                        mockk<ClassFilter> {
                            every { shouldTransform(className) } returns true
                            every { shouldTransform(superClassName) } returns true
                        }
                    }

                    single {
                        mockk<DynamicClassDefiner> {
                            every { defineClassForMethod("()V", false) } returns dynamicClassName
                            every { defineClassForMethod("(I)V", false) } returns dynamicClassName2
                        }
                    }
                }
            )

            val methodNode = makeMethodNode(
                0,
                "<init>",
                "()V",
                listOf(),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(MethodInsnNode(INVOKESPECIAL, superClassName, "<init>", "()V", false))
                    add(LineNumberNode(lineNumber2, LabelNode()))
                    add(MethodInsnNode(INVOKEVIRTUAL, superClassName, "someMethod", "(I)V", false))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Preamble for superclass ctor
                methodPreamble(className, lineNumber, "<init>")

                // Superclass ctor
                method(INVOKESPECIAL, superClassName, "<init>", "()V", false)

                // Record superclass trace after `this` is initialized
                startMethodTrace(dynamicClassName)
                receiver()
                endMethodTrace()

                lineNumber(lineNumber2)

                // Preamble for the next method like normal
                methodPreamble(className, lineNumber2, "someMethod")
            }
        }

        @Test
        fun `visit class initialization method`() {
            // A class initialization method is just like a static method with no args

            testKoin(
                module {
                    single {
                        mockk<ClassFilter> {
                            every { shouldTransform(className) } returns true
                        }
                    }

                    single {
                        mockk<DynamicClassDefiner> {
                            every { defineClassForMethod("()V", true) } returns dynamicClassName
                        }
                    }
                }
            )

            val methodNode = makeMethodNode(
                ACC_STATIC,
                "<clinit>",
                "()V",
                listOf(),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            checkInsns(methodNode.instructions) {
                startMethodTrace(dynamicClassName)
                endMethodTrace()

                lineNumber(lineNumber)
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

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Dup what's on the stack that will be stored
                insn(OpcodeUtil.getDupOpcode(opcode))

                varInsn(opcode, 1)

                // Load the context and trace the store
                recordStore(className, lineNumber, varName, localVariableDesc)
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

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            // No instrumentation for loads
            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)
                varInsn(opcode, 1)
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

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)
                iinc(1)

                // Load the value of the local after the increment
                varInsn(ILOAD, 1)

                // Load the context and trace the store
                recordStore(className, lineNumber, varName, "I")
            }
        }

        @Test
        fun `istore into a byte`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(makeLocalVariable(varName, "B", 1)),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(VarInsnNode(ISTORE, 1))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Dup what will be stored
                insn(DUP)

                // Do the store
                varInsn(ISTORE, 1)

                // Load the context and trace the store
                recordStore(className, lineNumber, varName, "B")
            }
        }

        @Test
        fun `astore a string`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(makeLocalVariable(varName, "Ljava/lang/String;", 1)),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(VarInsnNode(ASTORE, 1))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Dup what will be stored
                insn(DUP)

                // Do the store
                varInsn(ASTORE, 1)

                // Load the context and trace the store
                recordStore(className, lineNumber, varName, "Ljava/lang/Object;")
            }
        }

        @Test
        fun `istore after dstore`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(
                    makeLocalVariable(varName, "D", 1),
                    // Index 3 instead of 2 because doubles are category 2 types
                    makeLocalVariable(varName2, "I", 3)
                ),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(VarInsnNode(DSTORE, 1))
                    // Index 3 instead of 2 because doubles are category 2 types
                    add(VarInsnNode(ISTORE, 3))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Dup what will be stored
                insn(DUP2)

                // Do the store
                varInsn(DSTORE, 1)

                // Load the context and trace the store
                recordStore(className, lineNumber, varName, "D")

                // Dup what will be stored
                insn(DUP)

                // Do the store. Index 3 instead of 2 because doubles are category 2 types.
                varInsn(ISTORE, 3)

                // Load the context and trace the store
                recordStore(className, lineNumber, varName2, "I")
            }
        }

        @Test
        fun `store into int array`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(
                    makeLocalVariable(varName, "[I", 1)
                ),
                InsnList().apply {
                    // Generated from:
                    //   int[] i = new int[1];
                    //   i[0] = 6;
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(InsnNode(ICONST_0))
                    add(IntInsnNode(NEWARRAY, T_INT))
                    add(InsnNode(ICONST_0))
                    add(IntInsnNode(BIPUSH, 6))
                    add(InsnNode(IASTORE))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Keep the values on the stack that IASTORE needs
                insn(ICONST_0)
                intInsn(NEWARRAY, T_INT)
                insn(ICONST_0)
                intInsn(BIPUSH, 6)

                // But replace IASTORE with recording the store (which internally will do the store)
                recordArrayStore(className, lineNumber, "[I", "I")
            }
        }

        @Test
        fun `store into boolean array`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(
                    makeLocalVariable(varName, "[Z", 1)
                ),
                InsnList().apply {
                    // Generated from:
                    //   boolean[] b = new boolean[1];
                    //   b[0] = true;
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(InsnNode(ICONST_0))
                    add(IntInsnNode(NEWARRAY, T_BOOLEAN))
                    add(InsnNode(ICONST_0))
                    add(InsnNode(ICONST_1))
                    add(InsnNode(Opcodes.BASTORE))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                insn(ICONST_0)
                intInsn(NEWARRAY, T_BOOLEAN)
                insn(ICONST_0)
                insn(ICONST_1)
                recordBooleanOrByteArrayStore(className, lineNumber)
            }
        }

        @Test
        fun `store into new int array one-liner`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(
                    makeLocalVariable(varName, "[I", 1)
                ),
                InsnList().apply {
                    // Generated from:
                    //   int[] i = {6};
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(InsnNode(ICONST_1))
                    add(IntInsnNode(NEWARRAY, T_INT))
                    add(InsnNode(DUP))
                    add(InsnNode(ICONST_0))
                    add(IntInsnNode(BIPUSH, 6))
                    add(InsnNode(IASTORE))
                    add(VarInsnNode(ASTORE, 1))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Keep the values on the stack that IASTORE needs
                insn(ICONST_1)
                intInsn(NEWARRAY, T_INT)
                insn(DUP)
                insn(ICONST_0)
                intInsn(BIPUSH, 6)

                // But replace IASTORE with recording the store (which internally will do the store)
                recordArrayStore(className, lineNumber, "[I", "I")

                // The ASTORE also gets recorded
                insn(DUP)
                varInsn(ASTORE, 1)
                recordStore(className, lineNumber, varName, "Ljava/lang/Object;")
            }
        }

        @Test
        fun `store into new object array one-liner`() {
            testKoin(
                module {
                    single {
                        mockk<ClassFilter> {
                            every { shouldTransform("Ljava/lang/Object;") } returns false
                        }
                    }
                }
            )

            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(
                    makeLocalVariable(varName, "[Ljava/lang/Object;", 1)
                ),
                InsnList().apply {
                    // Generated from:
                    //   Object[] o = {new Object()};
                    //
                    //   ICONST_1
                    //   ANEWARRAY java/lang/Object
                    //   DUP
                    //   ICONST_0
                    //   NEW java/lang/Object
                    //   DUP
                    //   INVOKESPECIAL java/lang/Object.<init> ()V
                    //   AASTORE
                    //   ASTORE 1
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(InsnNode(ICONST_1))
                    add(TypeInsnNode(ANEWARRAY, "Ljava/lang/Object;"))
                    add(InsnNode(DUP))
                    add(InsnNode(ICONST_0))
                    add(TypeInsnNode(NEW, "Ljava/lang/Object;"))
                    add(InsnNode(DUP))
                    add(MethodInsnNode(INVOKESPECIAL, "Ljava/lang/Object;", "<init>", "()V", false))
                    add(InsnNode(AASTORE))
                    add(VarInsnNode(ASTORE, 1))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                insn(ICONST_1)
                typeInsn(ANEWARRAY, "Ljava/lang/Object;")
                insn(DUP)
                insn(ICONST_0)
                typeInsn(NEW, "Ljava/lang/Object;")
                insn(DUP)
                method(INVOKESPECIAL, "Ljava/lang/Object;", "<init>", "()V", false)

                // Replace the AASTORE with recording the store (which internally will do the store)
                recordArrayStore(className, lineNumber, "[Ljava/lang/Object;", "Ljava/lang/Object;")

                // DUP what will be stored
                insn(DUP)
                varInsn(ASTORE, 1)
                recordStore(className, lineNumber, varName, "Ljava/lang/Object;")
            }
        }

        @Test
        fun `store into 2d int array`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(
                    makeLocalVariable(varName, "[[I", 1)
                ),
                InsnList().apply {
                        /*
                        Generated from:
                        int[][] i = new int[1][1];
                        i[0][0] = 6;

                        ICONST_1
                        ICONST_1
                        MULTIANEWARRAY [[I 2
                        ASTORE 1
                        ALOAD 1
                        ICONST_0
                        AALOAD
                        ICONST_0
                        BIPUSH 6
                        IASTORE
                         */
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(InsnNode(ICONST_1))
                    add(InsnNode(ICONST_1))
                    add(MultiANewArrayInsnNode("[[I", 2))
                    add(VarInsnNode(ASTORE, 1))
                    add(LineNumberNode(lineNumber2, LabelNode()))
                    add(VarInsnNode(ALOAD, 1))
                    add(InsnNode(ICONST_0))
                    add(InsnNode(AALOAD))
                    add(InsnNode(ICONST_0))
                    add(IntInsnNode(BIPUSH, 6))
                    add(InsnNode(IASTORE))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)
                insn(ICONST_1)
                insn(ICONST_1)
                multiANewArrayInsn("[[I", 2)

                // The ASTORE also gets recorded
                insn(DUP)
                varInsn(ASTORE, 1)
                recordStore(className, lineNumber, varName, "Ljava/lang/Object;")

                // Keep the values on the stack that IASTORE needs
                lineNumber(lineNumber2)
                varInsn(ALOAD, 1)
                insn(ICONST_0)
                insn(AALOAD)
                insn(ICONST_0)
                intInsn(BIPUSH, 6)

                // But replace IASTORE with recording the store (which internally will do the store)
                recordArrayStore(className, lineNumber2, "[I", "I")
            }
        }

        @Test
        fun `store into 3d int array`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(
                    makeLocalVariable(varName, "[[[I", 1)
                ),
                InsnList().apply {
                        /*
                        Generated from:
                        int[][][] i = new int[1][1][1];
                        i[0][0][0] = 6;

                        ICONST_1
                        ICONST_1
                        ICONST_1
                        MULTIANEWARRAY [[[I 3
                        ASTORE 1
                        ALOAD 1
                        ICONST_0
                        AALOAD
                        ICONST_0
                        AALOAD
                        ICONST_0
                        BIPUSH 6
                        IASTORE
                         */
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(InsnNode(ICONST_1))
                    add(InsnNode(ICONST_1))
                    add(InsnNode(ICONST_1))
                    add(MultiANewArrayInsnNode("[[[I", 3))
                    add(VarInsnNode(ASTORE, 1))
                    add(LineNumberNode(lineNumber2, LabelNode()))
                    add(VarInsnNode(ALOAD, 1))
                    add(InsnNode(ICONST_0))
                    add(InsnNode(AALOAD))
                    add(InsnNode(ICONST_0))
                    add(InsnNode(AALOAD))
                    add(InsnNode(ICONST_0))
                    add(IntInsnNode(BIPUSH, 6))
                    add(InsnNode(IASTORE))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)
                insn(ICONST_1)
                insn(ICONST_1)
                insn(ICONST_1)
                multiANewArrayInsn("[[[I", 3)

                // The ASTORE also gets recorded
                insn(DUP)
                varInsn(ASTORE, 1)
                recordStore(className, lineNumber, varName, "Ljava/lang/Object;")

                // Keep the values on the stack that IASTORE needs
                lineNumber(lineNumber2)
                varInsn(ALOAD, 1)
                insn(ICONST_0)
                insn(AALOAD)
                insn(ICONST_0)
                insn(AALOAD)
                insn(ICONST_0)
                intInsn(BIPUSH, 6)

                // But replace IASTORE with recording the store (which internally will do the store)
                recordArrayStore(className, lineNumber2, "[I", "I")
            }
        }

        @Test
        fun `store into 3d int array one-liner`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(
                    makeLocalVariable(varName, "[[[I", 1)
                ),
                InsnList().apply {
                        /*
                        Generated from:
                        int[][][] i = {{{6}}};

                        ICONST_1
                        ANEWARRAY [[I
                        DUP
                        ICONST_0
                        ICONST_1
                        ANEWARRAY [I
                        DUP
                        ICONST_0
                        ICONST_1
                        NEWARRAY T_INT
                        DUP
                        ICONST_0
                        BIPUSH 6
                        IASTORE
                        AASTORE
                        AASTORE
                        ASTORE 1
                         */
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(InsnNode(ICONST_1))
                    add(TypeInsnNode(ANEWARRAY, "[[I"))
                    add(InsnNode(DUP))
                    add(InsnNode(ICONST_0))
                    add(InsnNode(ICONST_1))
                    add(TypeInsnNode(ANEWARRAY, "[I"))
                    add(InsnNode(DUP))
                    add(InsnNode(ICONST_0))
                    add(InsnNode(ICONST_1))
                    add(IntInsnNode(NEWARRAY, T_INT))
                    add(InsnNode(DUP))
                    add(InsnNode(ICONST_0))
                    add(IntInsnNode(BIPUSH, 6))
                    add(InsnNode(IASTORE))
                    add(InsnNode(AASTORE))
                    add(InsnNode(AASTORE))
                    add(VarInsnNode(ASTORE, 1))
                }
            )

            val classNode = makeClassNode(className, superClassName, methodNode)

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)
                insn(ICONST_1)
                typeInsn(ANEWARRAY, "[[I")
                insn(DUP)
                insn(ICONST_0)
                insn(ICONST_1)
                typeInsn(ANEWARRAY, "[I")
                insn(DUP)
                insn(ICONST_0)
                insn(ICONST_1)
                intInsn(NEWARRAY, T_INT)
                insn(DUP)
                insn(ICONST_0)
                intInsn(BIPUSH, 6)

                // Replace IASTORE with recording the store (which internally will do the store)
                recordArrayStore(className, lineNumber, "[I", "I")

                recordArrayStore(className, lineNumber, "[Ljava/lang/Object;", "Ljava/lang/Object;")

                recordArrayStore(className, lineNumber, "[Ljava/lang/Object;", "Ljava/lang/Object;")

                // Record the ASTORE. Dup what it stores.
                insn(DUP)
                varInsn(ASTORE, 1)
                recordStore(className, lineNumber, varName, "Ljava/lang/Object;")
            }
        }
    }

    @Nested
    inner class FieldTracing {

        @Test
        fun `put int field`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(FieldInsnNode(PUTFIELD, className, varName, "I"))
                }
            )

            val classNode = makeClassNode(
                className,
                superClassName,
                listOf(makeFieldNode(0, varName, "I")),
                methodNode
            )

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Dup what is on the stack and put it below both values for PUTFIELD
                insn(DUP_X1)

                // Emit the PUTFIELD
                field(PUTFIELD, className, varName, "I")

                // Record the put
                recordStore(className, lineNumber, "$className.$varName", "I")
            }
        }

        @Test
        fun `put long field`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(FieldInsnNode(PUTFIELD, className, varName, "J"))
                }
            )

            val classNode = makeClassNode(
                className,
                superClassName,
                listOf(makeFieldNode(0, varName, "J")),
                methodNode
            )

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Dup what is on the stack and put it below both values for PUTFIELD
                insn(DUP2_X1)

                // Emit the PUTFIELD
                field(PUTFIELD, className, varName, "J")

                // Record the put
                recordStore(className, lineNumber, "$className.$varName", "J")
            }
        }

        @Test
        fun `put static int field`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(FieldInsnNode(PUTSTATIC, className, varName, "I"))
                }
            )

            val classNode = makeClassNode(
                className,
                superClassName,
                listOf(makeFieldNode(ACC_STATIC, varName, "I")),
                methodNode
            )

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Dup what is on the stack and put it below the value for PUTSTATIC
                insn(DUP)

                // Emit the PUTSTATIC
                field(PUTSTATIC, className, varName, "I")

                // Record the put
                recordStore(className, lineNumber, "$className.$varName", "I")
            }
        }

        @Test
        fun `put static long field`() {
            val methodNode = makeMethodNode(
                0,
                methodName,
                "()V",
                listOf(),
                InsnList().apply {
                    add(LineNumberNode(lineNumber, LabelNode()))
                    add(FieldInsnNode(PUTSTATIC, className, varName, "J"))
                }
            )

            val classNode = makeClassNode(
                className,
                superClassName,
                listOf(makeFieldNode(ACC_STATIC, varName, "J")),
                methodNode
            )

            val transformer = OMJClassTransformer(
                classNode,
                // Recording method calls would make this test larger for no reason
                ClassTransformerOptions(recordMethodCall = false)
            )
            transformer.transform()

            checkInsns(methodNode.instructions) {
                lineNumber(lineNumber)

                // Dup what is on the stack and put it below the value for PUTSTATIC
                insn(DUP2)

                // Emit the PUTSTATIC
                field(PUTSTATIC, className, varName, "J")

                // Record the put
                recordStore(className, lineNumber, "$className.$varName", "J")
            }
        }
    }

    private fun makeClassNode(name: String, superName: String, method: MethodNode) =
        makeClassNode(name, superName, emptyList(), method)

    private fun makeClassNode(
        name: String,
        superName: String,
        fields: List<FieldNode>,
        method: MethodNode
    ) = ClassNode(ASM8).also {
        it.name = name
        it.superName = superName
        it.fields = fields
        it.methods.add(method)
        it.version = 51
    }

    private fun makeFieldNode(access: Int, name: String, desc: String) =
        FieldNode(ASM8, access, name, desc, null, null)

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

    private fun CheckInsns.methodPreamble(
        callerClass: String,
        lineNumber: Int,
        methodName: String
    ) {
        ldc(callerClass)
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
    }

    private fun CheckInsns.startMethodTrace(dynamicClassName: String) {
        type(NEW, dynamicClassName)
        insn(DUP)
        method(INVOKESPECIAL, dynamicClassName, "<init>", "()V", false)
        method(
            INVOKESTATIC,
            agentLibClassName,
            "methodCall_start",
            "(Lcom/octogonapus/omj/agentlib/MethodTrace;)V",
            false
        )
    }

    private fun CheckInsns.receiver() {
        varInsn(ALOAD, 0)
        method(
            INVOKESTATIC,
            agentLibClassName,
            "methodCall_argument_Object",
            "(Ljava/lang/Object;)V",
            false
        )
    }

    private fun CheckInsns.endMethodTrace() {
        method(INVOKESTATIC, agentLibClassName, "methodCall_end", "()V", false)
    }

    private fun CheckInsns.recordStore(
        className: String,
        lineNumber: Int,
        varName: String,
        descPrefix: String
    ) {
        ldc(className)
        ldc(lineNumber)
        ldc(varName)
        method(
            INVOKESTATIC,
            agentLibClassName,
            "store",
            "(${descPrefix}Ljava/lang/String;ILjava/lang/String;)V",
            false
        )
    }

    private fun CheckInsns.recordArrayStore(
        className: String,
        lineNumber: Int,
        arrayDesc: String,
        elemDesc: String
    ) {
        ldc(className)
        ldc(lineNumber)
        method(
            INVOKESTATIC,
            agentLibClassName,
            "store",
            "(${arrayDesc}I${elemDesc}Ljava/lang/String;I)V",
            false
        )
    }

    private fun CheckInsns.recordBooleanOrByteArrayStore(
        className: String,
        lineNumber: Int
    ) {
        ldc(className)
        ldc(lineNumber)
        method(
            INVOKESTATIC,
            agentLibClassName,
            "storeBooleanOrByteArray",
            "(Ljava/lang/Object;IBLjava/lang/String;I)V",
            false
        )
    }

    companion object {
        private const val className = "className"
        private const val superClassName = "superClassName"
        private const val methodName = "methodName"
        private const val methodOwner = "methodOwner"
        private const val varName = "varName"
        private const val varName2 = "varName2"
        private const val lineNumber = 3495
        private const val lineNumber2 = 8439
        private const val dynamicClassName = "dynamicClassName"
        private const val dynamicClassName2 = "dynamicClassName2"
    }
}

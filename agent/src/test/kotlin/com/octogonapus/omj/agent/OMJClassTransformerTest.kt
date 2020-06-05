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
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ASM8
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.DUP2
import org.objectweb.asm.Opcodes.DUP2_X1
import org.objectweb.asm.Opcodes.DUP_X1
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.Opcodes.NEW
import org.objectweb.asm.Opcodes.PUTFIELD
import org.objectweb.asm.Opcodes.PUTSTATIC
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
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
                local(ILOAD, 1)
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
                local(DLOAD, 0)
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
                local(ALOAD, 0)
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

                local(opcode, 1)

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
                local(ILOAD, 1)

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
                local(ISTORE, 1)

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
                local(ASTORE, 1)

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
                local(DSTORE, 1)

                // Load the context and trace the store
                recordStore(className, lineNumber, varName, "D")

                // Dup what will be stored
                insn(DUP)

                // Do the store. Index 3 instead of 2 because doubles are category 2 types.
                local(ISTORE, 3)

                // Load the context and trace the store
                recordStore(className, lineNumber, varName2, "I")
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
        local(ALOAD, 0)
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
        varDesc: String
    ) {
        ldc(className)
        ldc(lineNumber)
        ldc(varName)
        method(
            INVOKESTATIC,
            agentLibClassName,
            "store",
            "(${varDesc}Ljava/lang/String;ILjava/lang/String;)V",
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
        private const val lineNumber2 = 3895
        private const val dynamicClassName = "dynamicClassName"
        private const val dynamicClassName2 = "dynamicClassName2"
    }
}

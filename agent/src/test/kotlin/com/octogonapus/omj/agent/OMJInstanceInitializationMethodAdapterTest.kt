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

import arrow.core.Tuple4
import com.octogonapus.omj.testutil.KoinTestFixture
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ASM8
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LSTORE

internal class OMJInstanceInitializationMethodAdapterTest : KoinTestFixture() {

    companion object {
        private const val ctorBeingAdaptedDescriptor = "(I)V"
        private const val superClassCtorDescriptor = "()V"
        private const val superClassCtorName = "<init>"
        private const val className = "ClassName"
        private const val superName = "SuperName"
        private const val anotherClassName = "SomeClass"
        private const val anotherMethodName = "someMethod"
        private const val methodName = "methodName"
        private const val anotherMethodDescriptor = "(BZ)J"
        private const val lineNumber = 12945
    }

    @Nested
    inner class MethodTests {

        @Test
        fun `visit superclass constructor call`() {
            val (methodAdapter, superVisitor, methodAdapterUtil, dynamicClassDefiner) =
                getMethodAdapter()

            // Visit the super class's ctor
            methodAdapter.visitMethodInsn(
                INVOKESPECIAL,
                superName,
                superClassCtorName,
                superClassCtorDescriptor,
                false
            )

            verifySequence {
                // Call the super ctor first
                superVisitor.visitMethodInsn(
                    INVOKESPECIAL,
                    superName,
                    superClassCtorName,
                    superClassCtorDescriptor,
                    false
                )

                // Then record the trace after the super ctor
                methodAdapterUtil.visitMethodTrace(
                    superVisitor,
                    ctorBeingAdaptedDescriptor,
                    false,
                    dynamicClassDefiner
                )
            }
        }

        @Test
        fun `visit normal method call declared in a class that will be transformed`() {
            val (methodAdapter, superVisitor, methodAdapterUtil, _) = getMethodAdapter(
                module {
                    single {
                        mockk<ClassFilter> {
                            every { shouldTransform(any()) } returns true
                        }
                    }
                }
            )

            // Visit a line number before the method insn to simulate a class file with debug info
            methodAdapter.visitLineNumber(lineNumber, Label())

            // Visit a normal method call
            methodAdapter.visitMethodInsn(
                INVOKEVIRTUAL,
                anotherClassName,
                anotherMethodName,
                anotherMethodDescriptor,
                false
            )

            verifySequence {
                // Emit the line number
                superVisitor.visitLineNumber(lineNumber, any())

                // Emit the preamble
                methodAdapterUtil.visitMethodCallStartPreamble(
                    superVisitor,
                    lineNumber,
                    className,
                    anotherMethodName
                )

                // Emit the method call
                superVisitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    anotherClassName,
                    anotherMethodName,
                    anotherMethodDescriptor,
                    false
                )
            }
        }
    }

    @Nested
    inner class LoadAndStoreTests {

        @ParameterizedTest
        @ValueSource(
            ints = [
                ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, ILOAD, LLOAD, FLOAD, DLOAD,
                ALOAD
            ]
        )
        fun `visit opcode`(opcode: Int) {
            val (methodAdapter, superVisitor, methodAdapterUtil, _) = getMethodAdapter(
                module {
                    single<MethodsAndLocals>(named(METHODS_AND_LOCALS_NAME)) { mapOf() }
                }
            )

            // Visit a line number before the store to simulate a class file with debug info
            methodAdapter.visitLineNumber(lineNumber, Label())

            // Store into a local at index 1
            methodAdapter.visitVarInsn(opcode, 1)

            verifySequence {
                // Emit the line number
                superVisitor.visitLineNumber(lineNumber, any())

                methodAdapterUtil.visitVarInsn(
                    superVisitor,
                    className,
                    lineNumber,
                    opcode,
                    1,
                    null
                )
            }
        }

        @Test
        fun `visit iinc`() {
            val (methodAdapter, superVisitor, methodAdapterUtil, _) = getMethodAdapter(
                module {
                    single<MethodsAndLocals>(named(METHODS_AND_LOCALS_NAME)) { mapOf() }
                }
            )

            // Visit a line number before the store to simulate a class file with debug info
            methodAdapter.visitLineNumber(lineNumber, Label())

            // Increment a local at index 1 by 2
            methodAdapter.visitIincInsn(1, 2)

            verifySequence {
                // Emit the line number
                superVisitor.visitLineNumber(lineNumber, any())

                methodAdapterUtil.visitIincInsn(
                    visitor = superVisitor,
                    className = className,
                    lineNumber = lineNumber,
                    index = 1,
                    increment = 2,
                    locals = null
                )
            }
        }
    }

    private fun getMethodAdapter(
        additionalModule: Module = module { }
    ): Tuple4<OMJInstanceInitializationMethodAdapter, MethodVisitor, MethodAdapterUtil,
        DynamicClassDefiner> {
        val methodAdapterUtil = mockk<MethodAdapterUtil>(relaxed = true)
        val dynamicClassDefiner = mockk<DynamicClassDefiner>(relaxed = true)

        testKoin(
            module {
                single { methodAdapterUtil }
                single { dynamicClassDefiner }
            },
            additionalModule
        )

        val superVisitor = mockk<MethodVisitor>(relaxed = true)

        val methodAdapter = OMJInstanceInitializationMethodAdapter(
            ASM8,
            superVisitor,
            Method(methodName, ctorBeingAdaptedDescriptor),
            className,
            superName
        )

        return Tuple4(methodAdapter, superVisitor, methodAdapterUtil, dynamicClassDefiner)
    }
}

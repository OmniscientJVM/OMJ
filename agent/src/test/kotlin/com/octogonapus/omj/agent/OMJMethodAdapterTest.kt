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
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.koin.dsl.ModuleDeclaration
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
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LSTORE

internal class OMJMethodAdapterTest : KoinTestFixture() {

    companion object {
        private const val methodDescriptor = "()V"
        private const val className = "ClassName"
        private const val anotherClassName = "SomeClass"
        private const val anotherMethodName = "someMethod"
        private const val anotherMethodDescriptor = "(BZ)J"
        private const val lineNumber = 23498
    }

    @Nested
    inner class MethodTests {

        @Test
        fun `visit start of method`() {
            val (methodAdapter, superVisitor, methodAdapterUtil, dynamicClassDefiner) =
                getMethodAdapter { }

            methodAdapter.visitCode()

            verifySequence {
                // Start the code first
                superVisitor.visitCode()

                // Then record the arguments right after the signature, before any other bytecode
                methodAdapterUtil.recordMethodTrace(
                    superVisitor,
                    methodDescriptor,
                    false,
                    dynamicClassDefiner
                )
            }
        }

        @Test
        fun `visit a method insn contained in a class that will be transformed`() {
            val (methodAdapter, superVisitor, methodAdapterUtil, _) =
                getMethodAdapter {
                    single {
                        mockk<ClassFilter> {
                            every { shouldTransform(anotherClassName) } returns true
                        }
                    }
                }

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

                // The filter says we will transform the class containing the method, so emit a preamble
                methodAdapterUtil.visitMethodCallStartPreamble(
                    superVisitor,
                    lineNumber,
                    className,
                    anotherMethodName
                )

                // Emit the method insn
                superVisitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    anotherClassName,
                    anotherMethodName,
                    anotherMethodDescriptor,
                    false
                )
            }

            confirmVerified(methodAdapterUtil, superVisitor)
        }

        @Test
        fun `visit a method insn contained in a class that will not be transformed`() {
            val (methodAdapter, superVisitor, methodAdapterUtil, _) =
                getMethodAdapter {
                    single {
                        mockk<ClassFilter> {
                            every { shouldTransform(any()) } returns false
                        }
                    }
                }

            methodAdapter.visitMethodInsn(
                INVOKEVIRTUAL,
                anotherClassName,
                anotherMethodName,
                anotherMethodDescriptor,
                false
            )

            verifySequence {
                // Emit the method insn. No preamble because the method's containing class won't be
                // transformed.
                superVisitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    anotherClassName,
                    anotherMethodName,
                    anotherMethodDescriptor,
                    false
                )
            }

            confirmVerified(methodAdapterUtil, superVisitor)
        }
    }

    @Nested
    inner class StoreTests {

        @ParameterizedTest
        @ValueSource(ints = [ISTORE, LSTORE, FSTORE, DSTORE, ASTORE])
        fun `visit store`(storeOpcode: Int) {
            val (methodAdapter, superVisitor, methodAdapterUtil, _) = getMethodAdapter { }

            // Visit a line number before the store to simulate a class file with debug info
            methodAdapter.visitLineNumber(lineNumber, Label())

            // Store into a local at index 1
            methodAdapter.visitVarInsn(storeOpcode, 1)

            verifySequence {
                // Emit the line number
                superVisitor.visitLineNumber(lineNumber, any())

                // Trace it, which will emit the store on its own
                methodAdapterUtil.recordStore(
                    superVisitor,
                    className,
                    lineNumber,
                    storeOpcode,
                    1
                )
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [ILOAD, LLOAD, FLOAD, DLOAD, ALOAD])
        fun `visit load`(loadOpcode: Int) {
            val (methodAdapter, superVisitor, _, _) = getMethodAdapter { }

            // Load into local 1
            methodAdapter.visitVarInsn(loadOpcode, 1)

            verifySequence {
                // Emit the load; nothing else to do
                superVisitor.visitVarInsn(loadOpcode, 1)
            }
        }
    }

    private fun getMethodAdapter(
        isStatic: Boolean = false,
        addModules: ModuleDeclaration
    ): Tuple4<OMJMethodAdapter, MethodVisitor, MethodAdapterUtil,
        DynamicClassDefiner> {
        val methodAdapterUtil = mockk<MethodAdapterUtil>(relaxed = true)
        val dynamicClassDefiner = mockk<DynamicClassDefiner>(relaxed = true)
        testKoin {
            single { methodAdapterUtil }
            single { dynamicClassDefiner }
            addModules()
        }

        val superVisitor = mockk<MethodVisitor>(relaxed = true)

        val methodAdapter = OMJMethodAdapter(
            ASM8,
            superVisitor,
            methodDescriptor,
            isStatic,
            className
        )

        return Tuple4(methodAdapter, superVisitor, methodAdapterUtil, dynamicClassDefiner)
    }
}

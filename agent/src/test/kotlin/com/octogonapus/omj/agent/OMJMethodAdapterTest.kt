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

import com.octogonapus.omj.testutil.KoinTestFixture
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.jupiter.api.Test
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM8
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL

internal class OMJMethodAdapterTest : KoinTestFixture() {

    @Test
    fun `visit start of method`() {
        val methodDescriptor = "()V"
        val isStatic = false
        val className = "ClassName"

        val methodAdapterUtil = mockk<MethodAdapterUtil>(relaxed = true)
        val dynamicClassDefiner = mockk<DynamicClassDefiner>(relaxed = true)
        testKoin {
            single { methodAdapterUtil }
            single { dynamicClassDefiner }
        }

        val superVisitor = mockk<MethodVisitor>(relaxed = true)

        val methodAdapter = OMJMethodAdapter(
            ASM8,
            superVisitor,
            methodDescriptor,
            isStatic,
            className
        )

        methodAdapter.visitCode()

        verifySequence {
            // Start the code first
            superVisitor.visitCode()

            // Then record the arguments right after the signature, before any other bytecode
            methodAdapterUtil.recordMethodTrace(
                superVisitor,
                methodDescriptor,
                isStatic,
                dynamicClassDefiner
            )
        }
    }

    @Test
    fun `visit a method insn contained in a class that will be transformed`() {
        val methodOwner = "MethodOwner"
        val methodName = "methodName"

        val methodAdapterUtil = mockk<MethodAdapterUtil>(relaxed = true)
        val dynamicClassDefiner = mockk<DynamicClassDefiner>(relaxed = true)
        testKoin {
            single { methodAdapterUtil }
            single { dynamicClassDefiner }
            single {
                mockk<ClassFilter> {
                    every { shouldTransform(methodOwner) } returns true
                }
            }
        }

        val superVisitor = mockk<MethodVisitor>(relaxed = true)

        val currentClassName = ""
        val methodAdapter = OMJMethodAdapter(
            ASM8,
            superVisitor,
            "",
            false,
            currentClassName
        )

        // Visit a line number before the method insn to simulate a class file with debug info
        val lineNumber = 240
        methodAdapter.visitLineNumber(lineNumber, Label())

        val methodDescriptor = "()V"
        methodAdapter.visitMethodInsn(
            INVOKEVIRTUAL,
            methodOwner,
            methodName,
            methodDescriptor,
            false
        )

        verifySequence {
            // Emit the line number
            superVisitor.visitLineNumber(lineNumber, any())

            // The filter says we will transform the class containing the method, so emit a preamble
            methodAdapterUtil.visitMethodCallStartPreamble(
                superVisitor,
                lineNumber,
                currentClassName,
                methodName
            )

            // Emit the method insn
            superVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                methodOwner,
                methodName,
                methodDescriptor,
                false
            )
        }

        confirmVerified(methodAdapterUtil, superVisitor)
    }

    @Test
    fun `visit a method insn contained in a class that will not be transformed`() {
        val methodOwner = "MethodOwner"
        val methodName = "methodName"

        val methodAdapterUtil = mockk<MethodAdapterUtil>(relaxed = true)
        val dynamicClassDefiner = mockk<DynamicClassDefiner>(relaxed = true)
        testKoin {
            single { methodAdapterUtil }
            single { dynamicClassDefiner }
            single {
                mockk<ClassFilter> {
                    every { shouldTransform(any()) } returns false
                }
            }
        }

        val superVisitor = mockk<MethodVisitor>(relaxed = true)

        val currentClassName = ""
        val methodAdapter = OMJMethodAdapter(
            ASM8,
            superVisitor,
            "",
            false,
            currentClassName
        )

        val methodDescriptor = "()V"
        methodAdapter.visitMethodInsn(
            INVOKEVIRTUAL,
            methodOwner,
            methodName,
            methodDescriptor,
            false
        )

        verifySequence {
            // Emit the method insn
            superVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                methodOwner,
                methodName,
                methodDescriptor,
                false
            )
        }

        confirmVerified(methodAdapterUtil, superVisitor)
    }
}

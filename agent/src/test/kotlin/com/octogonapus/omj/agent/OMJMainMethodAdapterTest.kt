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
import com.octogonapus.omj.util.Util
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.jupiter.api.Test
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ASM8

internal class OMJMainMethodAdapterTest : KoinTestFixture() {

    companion object {
        private const val className = "ClassName"
        private const val anotherClassName = "SomeClass"
        private const val anotherMethodName = "someMethod"
        private const val anotherMethodDescriptor = "(BZ)J"
        private const val lineNumber = 1294
    }

    @Test
    fun `visit start of main method`() {
        val methodAdapterUtil = mockk<MethodAdapterUtil>(relaxed = true)
        val dynamicClassDefiner = mockk<DynamicClassDefiner>(relaxed = true)
        testKoin {
            single { methodAdapterUtil }
            single { dynamicClassDefiner }
        }

        val superVisitor = mockk<MethodVisitor>(relaxed = true)

        val methodAdapter = OMJMainMethodAdapter(ASM8, superVisitor, className)

        methodAdapter.visitCode()

        verifySequence {
            superVisitor.visitCode()

            // Emit a preamble and directly record it because we assume that there was no preamble
            // emitted for us (because this method is called by the JVM on startup)
            methodAdapterUtil.visitMethodCallStartPreamble(
                superVisitor,
                any(),
                className,
                "main"
            )
            methodAdapterUtil.recordMethodTrace(
                superVisitor,
                Util.mainMethodDescriptor,
                true,
                dynamicClassDefiner
            )
        }
    }

    @Test
    fun `visit a method insn contained in a class that will be transformed`() {
        val methodAdapterUtil = mockk<MethodAdapterUtil>(relaxed = true)
        val dynamicClassDefiner = mockk<DynamicClassDefiner>(relaxed = true)
        testKoin {
            single { methodAdapterUtil }
            single { dynamicClassDefiner }
            single {
                mockk<ClassFilter> {
                    every { shouldTransform(anotherClassName) } returns true
                }
            }
        }

        val superVisitor = mockk<MethodVisitor>(relaxed = true)

        val methodAdapter = OMJMainMethodAdapter(ASM8, superVisitor, className)

        // Visit a line number before the method insn to simulate a class file with debug info
        methodAdapter.visitLineNumber(lineNumber, Label())

        // Visit a normal method call
        methodAdapter.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
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
                Opcodes.INVOKEVIRTUAL,
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

        val methodAdapter = OMJMainMethodAdapter(ASM8, superVisitor, className)

        methodAdapter.visitMethodInsn(
            Opcodes.INVOKEVIRTUAL,
            anotherClassName,
            anotherMethodName,
            anotherMethodDescriptor,
            false
        )

        verifySequence {
            // Emit the method insn. No preamble because the method's containing class won't be
            // transformed.
            superVisitor.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                anotherClassName,
                anotherMethodName,
                anotherMethodDescriptor,
                false
            )
        }

        confirmVerified(methodAdapterUtil, superVisitor)
    }
}

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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifySequence
import org.junit.jupiter.api.Test
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM8
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKEVIRTUAL

internal class OMJInstanceInitializationMethodAdapterTest : KoinTestFixture() {

    companion object {
        private const val ctorBeingAdaptedDescriptor = "(I)V"
        private const val superClassCtorDescriptor = "()V"
        private const val superClassCtorName = "<init>"
        private const val className = "ClassName"
        private const val superName = "SuperName"
        private const val anotherClassName = "SomeClass"
        private const val anotherMethodName = "someMethod"
        private const val anotherMethodDescriptor = "(BZ)J"
    }

    @Test
    fun `visit superclass constructor call`() {
        val methodAdapterUtil = mockk<MethodAdapterUtil>(relaxed = true)
        val dynamicClassDefiner = mockk<DynamicClassDefiner>(relaxed = true)
        testKoin {
            single { methodAdapterUtil }
            single { dynamicClassDefiner }
        }

        val superVisitor = mockk<MethodVisitor>(relaxed = true)

        val methodAdapter = OMJInstanceInitializationMethodAdapter(
            ASM8,
            superVisitor,
            ctorBeingAdaptedDescriptor,
            className,
            superName
        )

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
            methodAdapterUtil.recordMethodTrace(
                superVisitor,
                ctorBeingAdaptedDescriptor,
                false,
                dynamicClassDefiner
            )
        }
    }

    @Test
    fun `visit normal method call declared in a class that will be transformed`() {
        val methodAdapterUtil = mockk<MethodAdapterUtil>(relaxed = true)
        val dynamicClassDefiner = mockk<DynamicClassDefiner>(relaxed = true)
        testKoin {
            single { methodAdapterUtil }
            single { dynamicClassDefiner }
            single {
                mockk<ClassFilter> {
                    every { shouldTransform(any()) } returns true
                }
            }
        }

        val superVisitor = mockk<MethodVisitor>(relaxed = true)

        val methodAdapter = OMJInstanceInitializationMethodAdapter(
            ASM8,
            superVisitor,
            ctorBeingAdaptedDescriptor,
            className,
            superName
        )

        // Visit a line number before the method insn to simulate a class file with debug info
        val lineNumber = 240
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

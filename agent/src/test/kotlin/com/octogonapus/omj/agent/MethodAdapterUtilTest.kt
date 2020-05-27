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
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import io.mockk.verifySequence
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.NEW

@Suppress("SameParameterValue")
internal class MethodAdapterUtilTest {

    @Nested
    inner class MethodCallStartPreambleTest {

        @Test
        fun `visit a preamble`() {
            val lineNumber = 2398
            val className = "ClassName"
            val methodName = "methodName"

            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().visitMethodCallStartPreamble(
                visitor,
                lineNumber,
                className,
                methodName
            )

            // Order is not important
            verifyAll {
                visitor.visitLdcInsn(className)
                visitor.visitMethodInsn(
                    INVOKESTATIC,
                    agentLibClassName,
                    "className",
                    "(Ljava/lang/String;)V",
                    false
                )

                visitor.visitLdcInsn(lineNumber)
                visitor.visitMethodInsn(
                    INVOKESTATIC,
                    agentLibClassName,
                    "lineNumber",
                    "(I)V",
                    false
                )

                visitor.visitLdcInsn(methodName)
                visitor.visitMethodInsn(
                    INVOKESTATIC,
                    agentLibClassName,
                    "methodName",
                    "(Ljava/lang/String;)V",
                    false
                )
            }
        }
    }

    @Nested
    inner class RecordMethodTraceTest {

        @Test
        fun `visit start of method with no args`() {
            val methodDescriptor = "()V"
            val isStatic = false
            val dynamicClassName = "DynamicClassName"
            val dynamicClassDefiner =
                dynamicClassDefiner(methodDescriptor, dynamicClassName, isStatic)

            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().recordMethodTrace(
                visitor,
                methodDescriptor,
                isStatic,
                dynamicClassDefiner
            )

            // Order is important
            verifySequence {
                startTrace(visitor, dynamicClassName)
                receiver(visitor)
                endTrace(visitor)
            }
        }

        @Test
        fun `visit start of static method with no args`() {
            val methodDescriptor = "()V"
            val isStatic = true
            val dynamicClassName = "DynamicClassName"
            val dynamicClassDefiner =
                dynamicClassDefiner(methodDescriptor, dynamicClassName, isStatic)

            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().recordMethodTrace(
                visitor,
                methodDescriptor,
                isStatic,
                dynamicClassDefiner
            )

            // Order is important
            verifySequence {
                startTrace(visitor, dynamicClassName)
                endTrace(visitor)
            }
        }

        @Test
        fun `visit start of method with one int arg`() {
            val methodDescriptor = "(I)V"
            val isStatic = false
            val dynamicClassName = "DynamicClassName"
            val dynamicClassDefiner =
                dynamicClassDefiner(methodDescriptor, dynamicClassName, isStatic)

            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().recordMethodTrace(
                visitor,
                methodDescriptor,
                isStatic,
                dynamicClassDefiner
            )

            // Order is important
            verifySequence {
                startTrace(visitor, dynamicClassName)
                receiver(visitor)
                intArg(visitor, 1) // Index 1 because there is a receiver
                endTrace(visitor)
            }
        }

        @Test
        fun `visit start of static method with one double arg`() {
            val methodDescriptor = "(D)V"
            val isStatic = true
            val dynamicClassName = "DynamicClassName"
            val dynamicClassDefiner =
                dynamicClassDefiner(methodDescriptor, dynamicClassName, isStatic)

            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().recordMethodTrace(
                visitor,
                methodDescriptor,
                isStatic,
                dynamicClassDefiner
            )

            // Order is important
            verifySequence {
                startTrace(visitor, dynamicClassName)
                doubleArg(visitor, 0) // Index 0 because there is no receiver
                endTrace(visitor)
            }
        }
    }

    private fun dynamicClassDefiner(
        methodDescriptor: String,
        dynamicClassName: String,
        isStatic: Boolean
    ): DynamicClassDefiner = mockk {
        every { defineClassForMethod(methodDescriptor, isStatic) } returns dynamicClassName
    }

    private fun startTrace(visitor: MethodVisitor, dynamicClassName: String) {
        visitor.visitTypeInsn(NEW, dynamicClassName)
        visitor.visitInsn(DUP)
        visitor.visitMethodInsn(INVOKESPECIAL, dynamicClassName, "<init>", "()V", false)
        visitor.visitMethodInsn(
            INVOKESTATIC,
            agentLibClassName,
            "methodCall_start",
            "(Lcom/octogonapus/omj/agentlib/MethodTrace;)V",
            false
        )
    }

    private fun receiver(visitor: MethodVisitor) {
        visitor.visitVarInsn(ALOAD, 0)
        visitor.visitMethodInsn(
            INVOKESTATIC,
            agentLibClassName,
            "methodCall_argument_Object",
            "(Ljava/lang/Object;)V",
            false
        )
    }

    private fun intArg(visitor: MethodVisitor, index: Int) {
        visitor.visitVarInsn(ILOAD, index)
        visitor.visitMethodInsn(
            INVOKESTATIC,
            agentLibClassName,
            "methodCall_argument_int",
            "(I)V",
            false
        )
    }

    private fun doubleArg(visitor: MethodVisitor, index: Int) {
        visitor.visitVarInsn(DLOAD, index)
        visitor.visitMethodInsn(
            INVOKESTATIC,
            agentLibClassName,
            "methodCall_argument_double",
            "(D)V",
            false
        )
    }

    private fun endTrace(visitor: MethodVisitor) {
        visitor.visitMethodInsn(
            INVOKESTATIC,
            agentLibClassName,
            "methodCall_end",
            "()V",
            false
        )
    }
}

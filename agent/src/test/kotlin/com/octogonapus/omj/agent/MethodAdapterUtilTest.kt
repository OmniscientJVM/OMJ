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
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.DUP2
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.Opcodes.NEW

@Suppress("SameParameterValue")
internal class MethodAdapterUtilTest {

    companion object {
        private const val lineNumber = 2398
        private const val className = "ClassName"
        private const val methodName = "methodName"
        private const val dynamicClassName = "DynamicClassName"
    }

    @Nested
    inner class MethodCallStartPreambleTest {

        @Test
        fun `visit a preamble`() {
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
            val dynamicClassDefiner =
                dynamicClassDefiner(methodDescriptor, dynamicClassName, isStatic)

            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().visitMethodTrace(
                visitor,
                methodDescriptor,
                isStatic,
                dynamicClassDefiner
            )

            // Order is important
            verifySequence {
                startMethodTrace(visitor, dynamicClassName)
                receiver(visitor)
                endMethodTrace(visitor)
            }
        }

        @Test
        fun `visit start of static method with no args`() {
            val methodDescriptor = "()V"
            val isStatic = true
            val dynamicClassDefiner =
                dynamicClassDefiner(methodDescriptor, dynamicClassName, isStatic)

            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().visitMethodTrace(
                visitor,
                methodDescriptor,
                isStatic,
                dynamicClassDefiner
            )

            // Order is important
            verifySequence {
                startMethodTrace(visitor, dynamicClassName)
                endMethodTrace(visitor)
            }
        }

        @Test
        fun `visit start of method with one int arg`() {
            val methodDescriptor = "(I)V"
            val isStatic = false
            val dynamicClassDefiner =
                dynamicClassDefiner(methodDescriptor, dynamicClassName, isStatic)

            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().visitMethodTrace(
                visitor,
                methodDescriptor,
                isStatic,
                dynamicClassDefiner
            )

            // Order is important
            verifySequence {
                startMethodTrace(visitor, dynamicClassName)
                receiver(visitor)
                intArg(visitor, 1) // Index 1 because there is a receiver
                endMethodTrace(visitor)
            }
        }

        @Test
        fun `visit start of static method with one double arg`() {
            val methodDescriptor = "(D)V"
            val isStatic = true
            val dynamicClassDefiner =
                dynamicClassDefiner(methodDescriptor, dynamicClassName, isStatic)

            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().visitMethodTrace(
                visitor,
                methodDescriptor,
                isStatic,
                dynamicClassDefiner
            )

            // Order is important
            verifySequence {
                startMethodTrace(visitor, dynamicClassName)
                doubleArg(visitor, 0) // Index 0 because there is no receiver
                endMethodTrace(visitor)
            }
        }
    }

    @Nested
    inner class RecordStoreTest {

        @Test
        fun `visit istore`() {
            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().visitVarInsn(
                visitor,
                className,
                lineNumber,
                ISTORE,
                1,
                null
            )

            verifySequence {
                // Save what will be stored
                visitor.visitInsn(DUP)

                // Store it
                visitor.visitVarInsn(ISTORE, 1)

                // Class name
                visitor.visitLdcInsn(className)

                // Line number
                visitor.visitLdcInsn(lineNumber)

                // Record the store
                visitor.visitMethodInsn(
                    INVOKESTATIC,
                    agentLibClassName,
                    "store",
                    "(ILjava/lang/String;I)V",
                    false
                )
            }
        }

        @Test
        fun `visit istore into a byte`() {
            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().visitVarInsn(
                visitor,
                className,
                lineNumber,
                ISTORE,
                1,
                listOf(LocalVariable("b", "B", 1)) // byte at index 1
            )

            verifySequence {
                // Save what will be stored
                visitor.visitInsn(DUP)

                // Store it
                visitor.visitVarInsn(ISTORE, 1)

                // Class name
                visitor.visitLdcInsn(className)

                // Line number
                visitor.visitLdcInsn(lineNumber)

                // Record the store
                visitor.visitMethodInsn(
                    INVOKESTATIC,
                    agentLibClassName,
                    "store",
                    "(BLjava/lang/String;I)V", // Should have pulled the descriptor from the locals
                    false
                )
            }
        }

        @Test
        fun `visit lstore`() {
            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().visitVarInsn(
                visitor,
                className,
                lineNumber,
                LSTORE,
                1,
                null
            )

            verifySequence {
                // Save what will be stored
                visitor.visitInsn(DUP2)

                // Store it
                visitor.visitVarInsn(LSTORE, 1)

                // Class name
                visitor.visitLdcInsn(className)

                // Line number
                visitor.visitLdcInsn(lineNumber)

                // Record the store
                visitor.visitMethodInsn(
                    INVOKESTATIC,
                    agentLibClassName,
                    "store",
                    "(JLjava/lang/String;I)V",
                    false
                )
            }
        }

        @Test
        fun `visit aload`() {
            val visitor = mockk<MethodVisitor>(relaxed = true)
            MethodAdapterUtil().visitVarInsn(
                visitor,
                className,
                lineNumber,
                ALOAD,
                1,
                null
            )

            verifySequence {
                // Nothing to do for non-store insn
                visitor.visitVarInsn(ALOAD, 1)
            }
        }

        @Test
        fun `visit istore after dstore`() {
            val secondLineNumber = lineNumber + 1
            val visitor = mockk<MethodVisitor>(relaxed = true)
            val util = MethodAdapterUtil()
            val locals = listOf(
                LocalVariable("d", "D", 1),
                LocalVariable("i", "I", 3) // 3 instead of 2 because doubles take up two slots
            )

            util.visitVarInsn(
                visitor,
                className,
                lineNumber,
                DSTORE,
                1,
                locals
            )

            util.visitVarInsn(
                visitor,
                className,
                secondLineNumber,
                ISTORE,
                3,
                locals
            )

            verifySequence {
                // /////////////////////////////
                // DSTORE
                // /////////////////////////////

                // Save what will be stored
                visitor.visitInsn(DUP2)

                // Store it
                visitor.visitVarInsn(DSTORE, 1)

                // Class name
                visitor.visitLdcInsn(className)

                // Line number
                visitor.visitLdcInsn(lineNumber)

                // Record the store
                visitor.visitMethodInsn(
                    INVOKESTATIC,
                    agentLibClassName,
                    "store",
                    "(DLjava/lang/String;I)V",
                    false
                )

                // /////////////////////////////
                // ISTORE
                // /////////////////////////////

                // Save what will be stored
                visitor.visitInsn(DUP)

                // Store it
                visitor.visitVarInsn(ISTORE, 3)

                // Class name
                visitor.visitLdcInsn(className)

                // Line number
                visitor.visitLdcInsn(secondLineNumber)

                // Record the store
                visitor.visitMethodInsn(
                    INVOKESTATIC,
                    agentLibClassName,
                    "store",
                    "(ILjava/lang/String;I)V", // Should have pulled the descriptor from the locals
                    false
                )
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

    private fun startMethodTrace(visitor: MethodVisitor, dynamicClassName: String) {
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

    private fun endMethodTrace(visitor: MethodVisitor) {
        visitor.visitMethodInsn(
            INVOKESTATIC,
            agentLibClassName,
            "methodCall_end",
            "()V",
            false
        )
    }
}

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

import mu.KotlinLogging
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal object MethodAdapterUtil {

    private val logger = KotlinLogging.logger { }

    /**
     * Records contextual information about a method call. This should be used before the method
     * call happens, when visiting method instructions.
     *
     * CRITICAL METHOD CONTRACT: THIS METHOD DOES NOT LEAVE A LASTING EFFECT ON THE STACK. All data
     * this method loads onto the stack is removed by the end of its bytecode.
     *
     * @receiver The method visitor to delegate to.
     * @param currentLineNumber The most up-to-date line number from the method visitor.
     * @param fullyQualifiedClassName The fully-qualified name of the class the method call happens
     * in.
     * @param name The name of the method being called.
     */
    internal fun MethodVisitor.visitMethodCallStartPreamble(
        currentLineNumber: Int,
        fullyQualifiedClassName: String,
        name: String
    ) {
        visitLdcInsn(fullyQualifiedClassName)
        visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/octogonapus/omj/agentlib/OMJAgentLib",
            "className",
            "(Ljava/lang/String;)V",
            false
        )

        visitLdcInsn(currentLineNumber)
        visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/octogonapus/omj/agentlib/OMJAgentLib",
            "lineNumber",
            "(I)V",
            false
        )

        visitLdcInsn(name)
        visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/octogonapus/omj/agentlib/OMJAgentLib",
            "methodName",
            "(Ljava/lang/String;)V",
            false
        )
    }

    /**
     * Generates a method trace container class, makes a new instance of it, records all of the
     * method's arguments, and gives the trace to the agent-lib.
     *
     * CRITICAL METHOD CONTRACT: THIS METHOD DOES NOT LEAVE A LASTING EFFECT ON THE STACK. All data
     * this method loads onto the stack is removed by the end of its bytecode.
     *
     * @receiver The method visitor to delegate to.
     * @param methodDescriptor The method's descriptor.
     * @param isStatic True if the method is static.
     * @param dynamicClassDefiner The [DynamicClassDefiner] to use to generate the method trace
     * container class.
     */
    internal fun MethodVisitor.recordMethodTrace(
        methodDescriptor: String,
        isStatic: Boolean,
        dynamicClassDefiner: DynamicClassDefiner
    ) {
        // Generate a method trace container class and make a new instance of it. Contextual information
        // about the is passed to the agent lib earlier when method instructions are visited using
        // `visitMethodCallStartPreamble`.
        val dynamicClassName = dynamicClassDefiner.defineClassForMethod(methodDescriptor, isStatic)
        visitTypeInsn(Opcodes.NEW, dynamicClassName)
        visitInsn(Opcodes.DUP)
        visitMethodInsn(Opcodes.INVOKESPECIAL, dynamicClassName, "<init>", "()V", false)
        visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/octogonapus/omj/agentlib/OMJAgentLib",
            "methodCall_start",
            "(Lcom/octogonapus/omj/agentlib/MethodTrace;)V",
            false
        )

        val argumentTypes = Type.getArgumentTypes(methodDescriptor)
        logger.debug { "argumentTypes = ${argumentTypes?.contentDeepToString()}" }

        val virtualOffset = if (isStatic) 0 else 1
        if (!isStatic) {
            visitVarInsn(Opcodes.ALOAD, 0)
            visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/octogonapus/omj/agentlib/OMJAgentLib",
                "methodCall_argument_Object",
                "(Ljava/lang/Object;)V",
                false
            )
        }

        for (i in argumentTypes.indices) {
            val argumentType = argumentTypes[i]
            val methodName = "methodCall_argument_" + TypeUtil.getAdaptedClassName(argumentType)
            val methodDesc = "(" + TypeUtil.getAdaptedDescriptor(argumentType) + ")V"

            logger.debug {
                """
                Generated methodCall_argument_xxx override
                methodName = $methodName
                methodDescriptor = $methodDesc
                """.trimIndent()
            }

            visitVarInsn(argumentType.getOpcode(Opcodes.ILOAD), i + virtualOffset)
            visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/octogonapus/omj/agentlib/OMJAgentLib",
                methodName,
                methodDesc,
                false
            )
        }

        visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "com/octogonapus/omj/agentlib/OMJAgentLib",
            "methodCall_end",
            "()V",
            false
        )
    }

    /**
     * Converts a "path-type" class name (e.g., `com/octogonapus/omj/MyClass`) to a "package-type"
     * class name (e.g., `com.octogonapus.omj.MyClass`).
     *
     * @param currentClassName The path-type class name to convert.
     * @return The package-type version of the [currentClassName].
     */
    fun convertPathTypeToPackageType(currentClassName: String): String {
        val indexOfLastSeparator = currentClassName.lastIndexOf('/') + 1
        val packagePrefix = currentClassName.substring(0, indexOfLastSeparator)
        val className = currentClassName.substring(indexOfLastSeparator)
        return packagePrefix.replace('/', '.') + className
    }
}

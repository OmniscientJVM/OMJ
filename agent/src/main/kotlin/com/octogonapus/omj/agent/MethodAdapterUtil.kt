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
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.IINC
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.Opcodes.NEW
import org.objectweb.asm.Type

internal class MethodAdapterUtil {

    /**
     * Records contextual information about a method call. This should be used before the method
     * call happens, when visiting method instructions.
     *
     * CRITICAL METHOD CONTRACT: THIS METHOD DOES NOT LEAVE A LASTING EFFECT ON THE STACK. All data
     * this method loads onto the stack is removed by the end of its bytecode.
     *
     * @param methodVisitor The method visitor to delegate to.
     * @param currentLineNumber The most up-to-date line number from the method visitor.
     * @param fullyQualifiedClassName The fully-qualified name of the class the method call happens
     * in.
     * @param name The name of the method being called.
     */
    internal fun visitMethodCallStartPreamble(
        methodVisitor: MethodVisitor,
        currentLineNumber: Int,
        fullyQualifiedClassName: String,
        name: String
    ) {
        with(methodVisitor) {
            visitLdcInsn(fullyQualifiedClassName)
            visitMethodInsn(
                INVOKESTATIC,
                agentLibClassName,
                "className",
                "(Ljava/lang/String;)V",
                false
            )

            visitLdcInsn(currentLineNumber)
            visitMethodInsn(
                INVOKESTATIC,
                agentLibClassName,
                "lineNumber",
                "(I)V",
                false
            )

            visitLdcInsn(name)
            visitMethodInsn(
                INVOKESTATIC,
                agentLibClassName,
                "methodName",
                "(Ljava/lang/String;)V",
                false
            )
        }
    }

    /**
     * Generates a method trace container class, makes a new instance of it, records all of the
     * method's arguments, and gives the trace to the agent-lib.
     *
     * CRITICAL METHOD CONTRACT: THIS METHOD DOES NOT LEAVE A LASTING EFFECT ON THE STACK. All data
     * this method loads onto the stack is removed by the end of its bytecode.
     *
     * @param methodVisitor The method visitor to delegate to.
     * @param methodDescriptor The method's descriptor.
     * @param isStatic True if the method is static.
     * @param dynamicClassDefiner The [DynamicClassDefiner] to use to generate the method trace
     * container class.
     */
    internal fun visitMethodTrace(
        methodVisitor: MethodVisitor,
        methodDescriptor: String,
        isStatic: Boolean,
        dynamicClassDefiner: DynamicClassDefiner
    ) {
        with(methodVisitor) {
            // Generate a method trace container class and make a new instance of it. Contextual
            // information about the is passed to the agent lib earlier when method instructions are
            // visited using `visitMethodCallStartPreamble`.
            val dynamicClassName =
                dynamicClassDefiner.defineClassForMethod(methodDescriptor, isStatic)
            visitTypeInsn(NEW, dynamicClassName)
            visitInsn(DUP)
            visitMethodInsn(INVOKESPECIAL, dynamicClassName, "<init>", "()V", false)
            visitMethodInsn(
                INVOKESTATIC,
                agentLibClassName,
                "methodCall_start",
                "(Lcom/octogonapus/omj/agentlib/MethodTrace;)V",
                false
            )

            // Compute the stack index of each argument type. We can't use the list index as the
            // stack index because some types take up two indices. Start `stackIndex` at `0` even if
            // the method is virtual because the virtual offset is handled later.
            var stackIndex = 0
            val argumentTypes = Type.getArgumentTypes(methodDescriptor).map { type ->
                val oldStackIndex = stackIndex
                stackIndex += TypeUtil.getStackSize(type)
                type to oldStackIndex
            }

            logger.debug { "argumentTypes = ${argumentTypes.joinToString()}" }

            val virtualOffset = if (isStatic) 0 else 1
            if (!isStatic) {
                visitVarInsn(ALOAD, 0)
                visitMethodInsn(
                    INVOKESTATIC,
                    agentLibClassName,
                    "methodCall_argument_Object",
                    "(Ljava/lang/Object;)V",
                    false
                )
            }

            argumentTypes.forEach { (argumentType, stackIndex) ->
                val methodName = "methodCall_argument_" + TypeUtil.getAdaptedClassName(argumentType)
                val methodDesc = "(" + TypeUtil.getAdaptedDescriptor(argumentType) + ")V"

                logger.debug {
                    """
                    Generated methodCall_argument_xxx override
                    methodName = $methodName
                    methodDescriptor = $methodDesc
                    """.trimIndent()
                }

                visitVarInsn(argumentType.getOpcode(ILOAD), stackIndex + virtualOffset)
                visitMethodInsn(
                    INVOKESTATIC,
                    agentLibClassName,
                    methodName,
                    methodDesc,
                    false
                )
            }

            visitMethodInsn(
                INVOKESTATIC,
                agentLibClassName,
                "methodCall_end",
                "()V",
                false
            )
        }
    }

    /**
     * Visits a var insn. If it is a *STORE insn, then that store is recorded.
     *
     * @param methodVisitor The method visitor to delegate to.
     * @param className The name of the class the store is in.
     * @param lineNumber The line number of the store.
     * @param opcode The opcode being visited.
     * @param index The index of the local variable (the operand of the insn).
     * @param locals A list of all the locals in the surrounding method, or `null` if none were
     * visited.
     */
    internal fun visitVarInsn(
        methodVisitor: MethodVisitor,
        className: String,
        lineNumber: Int,
        opcode: Int,
        index: Int,
        locals: List<LocalVariable>?
    ) {
        when (opcode) {
            ISTORE, LSTORE, FSTORE, DSTORE, ASTORE -> with(methodVisitor) {
                visitInsn(OpcodeUtil.getDupOpcode(opcode))

                visitVarInsn(opcode, index)

                recordStore(locals, index, opcode, className, lineNumber)
            }

            else -> methodVisitor.visitVarInsn(opcode, index)
        }
    }

    /**
     * Visits an IINC insn to record it.
     *
     * @param visitor The method visitor to delegate to.
     * @param className The name of the class the store is in.
     * @param lineNumber The line number of the store.
     * @param index The index of the local variable (the operand of the insn).
     * @param increment The amount to increment by (the second operand of the insn).
     * @param locals A list of all the locals in the surrounding method, or `null` if none were
     * visited.
     */
    fun visitIincInsn(
        visitor: MethodVisitor,
        className: String,
        lineNumber: Int,
        index: Int,
        increment: Int,
        locals: List<LocalVariable>?
    ) {
        val localVariable = getLocalVariable(locals, index, IINC)
        check(localVariable.descriptor == "I") {
            """
            The local being incremented was not an int! What is being incremented?
            localVariable=$localVariable
            """.trimIndent()
        }

        with(visitor) {
            visitIincInsn(index, increment)

            visitVarInsn(ILOAD, index)

            // Pretend like it was an ISTORE
            recordStore(locals, index, ISTORE, className, lineNumber)
        }
    }

    private fun MethodVisitor.recordStore(
        locals: List<LocalVariable>?,
        index: Int,
        opcode: Int,
        className: String,
        lineNumber: Int
    ) {
        val localVariable = getLocalVariable(locals, index, opcode)

        visitLdcInsn(className)
        visitLdcInsn(lineNumber)
        visitLdcInsn(localVariable.name)
        visitMethodInsn(
            INVOKESTATIC,
            agentLibClassName,
            "store",
            "(${localVariable.descriptor}Ljava/lang/String;ILjava/lang/String;)V",
            false
        )
    }

    private fun getLocalVariable(locals: List<LocalVariable>?, index: Int, opcode: Int) =
        if (locals != null) {
            val localVariable = locals.first { it.index == index }

            // Locals were gathered earlier, so we can use them for more information, except if
            // the local is a type of object. All objects should use the same object descriptor.
            if (opcode == ASTORE) {
                // So, if we have an object, enforce the object descriptor.
                localVariable.copy(descriptor = OpcodeUtil.getStoreDescriptor(opcode))
            } else {
                localVariable
            }
        } else {
            // No locals, so make a best guess using the opcode.
            LocalVariable(
                name = "UNKNOWN",
                descriptor = OpcodeUtil.getStoreDescriptor(opcode),
                index = index
            )
        }

    companion object {

        private val logger = KotlinLogging.logger { }
        const val agentLibClassName = "com/octogonapus/omj/agentlib/OMJAgentLib"

        /**
         * Converts a "path-type" class name (e.g., `com/octogonapus/omj/MyClass`) to a
         * "package-type" class name (e.g., `com.octogonapus.omj.MyClass`).
         *
         * @param currentClassName The path-type class name to convert.
         * @return The package-type version of the [currentClassName].
         */
        internal fun convertPathTypeToPackageType(currentClassName: String): String {
            val indexOfLastSeparator = currentClassName.lastIndexOf('/') + 1
            val packagePrefix = currentClassName.substring(0, indexOfLastSeparator)
            val className = currentClassName.substring(indexOfLastSeparator)
            return packagePrefix.replace('/', '.') + className
        }
    }
}

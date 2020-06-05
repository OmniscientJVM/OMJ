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
import com.octogonapus.omj.agent.MethodAdapterUtil.Companion.convertPathTypeToPackageType
import com.octogonapus.omj.di.OMJKoinComponent
import mu.KotlinLogging
import org.koin.core.inject
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.Opcodes.NEW
import org.objectweb.asm.Opcodes.PUTFIELD
import org.objectweb.asm.Opcodes.PUTSTATIC
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.LocalVariableNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.VarInsnNode

/**
 * @param recordMethodCall If true, then the body of methods will be instrumented to record
 * method calls.
 */
data class ClassTransformerOptions(
    val recordMethodCall: Boolean = true
)

/**
 * Instruments a class.
 *
 * @param classNode The class to instrument.
 * @param options Various options to control what parts of the class get instrumented.
 */
internal class OMJClassTransformer(
    private val classNode: ClassNode,
    private val options: ClassTransformerOptions = ClassTransformerOptions()
) : OMJKoinComponent {

    private val dynamicClassDefiner by inject<DynamicClassDefiner>()
    private val classFilter by inject<ClassFilter>()
    private val fullyQualifiedClassName =
        MethodAdapterUtil.convertPathTypeToPackageType(classNode.name)

    fun transform() {
        // If superName is null, we are visiting the Object class, so there is nothing for us to
        // instrument. Otherwise, instrument the class.
        if (classNode.superName != null) {
            instrumentClass(classNode)
        }
    }

    private fun instrumentClass(classNode: ClassNode) {
        classNode.methods.forEach { methodNode ->
            instrumentMethod(methodNode)
        }
    }

    private fun instrumentMethod(methodNode: MethodNode) {
        val insertions = when {
            methodNode.isInstanceInitializationMethod() ->
                instrumentInstanceInitializationMethod(methodNode)

            methodNode.isClassInitializationMethod() ->
                instrumentClassInitializationMethod(methodNode)

            methodNode.isMainMethod() -> instrumentMainMethod(methodNode)

            else -> instrumentNormalMethod(methodNode, options)
        }

        insertions.forEach { it.insert() }
    }

    private fun instrumentInstanceInitializationMethod(
        methodNode: MethodNode
    ): List<InsnListInsertion> {
        // Ensure the superclass ctor is the first method insn
        val methodInsns = methodNode.instructions.mapNotNull { it as? MethodInsnNode }
        val firstMethodInsn = methodInsns.first()
        check(
            firstMethodInsn.let {
                it.opcode == INVOKESPECIAL && it.name == "<init>" && it.owner == classNode.superName
            }
        ) {
            "The first method insn was not the superclass instance initializer: $firstMethodInsn"
        }

        // Record this method call after the superclass instance initialization method has been
        // called, because before that, `this` is uninitialized.
        return listOf(
            methodNode.instructions.insertAfter(firstMethodInsn) {
                recordMethodCall(methodNode)
            }
        ) + instrumentNormalMethod(
            methodNode,
            // Don't record the method call because we just did that in here
            options.copy(recordMethodCall = false)
        )
    }

    private fun instrumentClassInitializationMethod(methodNode: MethodNode) =
        instrumentNormalMethod(methodNode, options)

    private fun instrumentMainMethod(methodNode: MethodNode): List<InsnListInsertion> {
        val firstLineNumber = methodNode.instructions
            .mapNotNull { it as? LineNumberNode }
            .firstOrNull()
            ?.line
            ?: 0

        return listOf(
            methodNode.instructions.insertBefore(methodNode.instructions.first) {
                emitPreamble(firstLineNumber, methodNode.name)
            }
        ) + instrumentNormalMethod(methodNode, options)
    }

    private fun instrumentNormalMethod(
        methodNode: MethodNode,
        options: ClassTransformerOptions
    ): List<InsnListInsertion> {
        var currentLineNumber = LineNumberNode(0, LabelNode())

        val insertions = methodNode.instructions.flatMap {
            when (it) {
                is LineNumberNode -> {
                    currentLineNumber = it
                    emptyList()
                }

                is MethodInsnNode -> if (classFilter.shouldTransform(it.owner)) {
                    instrumentMethodInsn(methodNode, it, currentLineNumber.line)
                } else emptyList()

                is VarInsnNode -> when (it.opcode) {
                    ISTORE, LSTORE, FSTORE, DSTORE, ASTORE ->
                        instrumentVarInsn(methodNode, it, currentLineNumber.line)
                    else -> emptyList()
                }

                is IincInsnNode -> instrumentIincInsn(methodNode, it, currentLineNumber.line)

                is FieldInsnNode -> when (it.opcode) {
                    PUTFIELD, PUTSTATIC -> instrumentPutInsn(methodNode, it, currentLineNumber.line)
                    else -> emptyList()
                }

                else -> emptyList()
            }
        }

        val bodyInstrumentation =
            if (options.recordMethodCall) instrumentMethodBody(methodNode)
            else emptyList()

        return bodyInstrumentation + insertions
    }

    private fun instrumentPutInsn(
        methodNode: MethodNode,
        fieldInsnNode: FieldInsnNode,
        lineNumber: Int
    ) = listOf(
        methodNode.instructions.insertBefore(fieldInsnNode) {
            add(InsnNode(OpcodeUtil.getDupOpcode(fieldInsnNode.opcode, fieldInsnNode.desc)))
        },
        methodNode.instructions.insertAfter(fieldInsnNode) {
            recordStore(lineNumber, fieldInsnNode)
        }
    )

    private fun instrumentMethodBody(methodNode: MethodNode) = listOf(
        methodNode.instructions.insertBefore(methodNode.instructions.first) {
            recordMethodCall(methodNode)
        }
    )

    private fun InsnList.recordMethodCall(methodNode: MethodNode) {
        val isStatic = hasAccessFlag(methodNode.access, ACC_STATIC)
        val dynamicClassName = dynamicClassDefiner.defineClassForMethod(methodNode.desc, isStatic)

        // Generate a method trace container class and make a new instance of it.
        // Contextual information about this method is passed to the agent lib earlier
        // when method instructions are visited.
        add(TypeInsnNode(NEW, dynamicClassName))
        add(InsnNode(DUP))
        add(MethodInsnNode(INVOKESPECIAL, dynamicClassName, "<init>", "()V", false))
        add(
            MethodInsnNode(
                INVOKESTATIC,
                agentLibClassName,
                "methodCall_start",
                "(Lcom/octogonapus/omj/agentlib/MethodTrace;)V",
                false
            )
        )

        // Compute the stack index of each argument type. We can't use the list index as the
        // stack index because some types take up two indices. Start `stackIndex` at `0`
        // even if the method is virtual because the virtual offset is handled later.
        var stackIndex = 0
        val argumentTypes = Type.getArgumentTypes(methodNode.desc).map { type ->
            val oldStackIndex = stackIndex
            stackIndex += TypeUtil.getStackSize(type)
            type to oldStackIndex
        }

        logger.debug { "argumentTypes = ${argumentTypes.joinToString()}" }

        val virtualOffset = if (isStatic) 0 else 1
        if (!isStatic) {
            add(VarInsnNode(ALOAD, 0))
            add(
                MethodInsnNode(
                    INVOKESTATIC,
                    agentLibClassName,
                    "methodCall_argument_Object",
                    "(Ljava/lang/Object;)V",
                    false
                )
            )
        }

        argumentTypes.forEach { (argumentType, stackIndex) ->
            val methodName = "methodCall_argument_" +
                TypeUtil.getAdaptedClassName(argumentType)
            val methodDesc = "(" + TypeUtil.getAdaptedDescriptor(argumentType) + ")V"

            logger.debug {
                """
                Generated methodCall_argument_xxx override
                methodName = $methodName
                methodDescriptor = $methodDesc
                """.trimIndent()
            }

            add(VarInsnNode(argumentType.getOpcode(ILOAD), stackIndex + virtualOffset))
            add(
                MethodInsnNode(
                    INVOKESTATIC,
                    agentLibClassName,
                    methodName,
                    methodDesc,
                    false
                )
            )
        }

        add(
            MethodInsnNode(
                INVOKESTATIC,
                agentLibClassName,
                "methodCall_end",
                "()V",
                false
            )
        )
    }

    private fun instrumentMethodInsn(
        methodNode: MethodNode,
        methodInsnNode: MethodInsnNode,
        lineNumber: Int
    ) = listOf(
        methodNode.instructions.insertBefore(methodInsnNode) {
            emitPreamble(lineNumber, methodInsnNode.name)
        }
    )

    private fun InsnList.emitPreamble(lineNumber: Int, methodName: String) {
        add(LdcInsnNode(fullyQualifiedClassName))
        add(
            MethodInsnNode(
                INVOKESTATIC,
                agentLibClassName,
                "className",
                "(Ljava/lang/String;)V",
                false
            )
        )

        add(LdcInsnNode(lineNumber))
        add(
            MethodInsnNode(
                INVOKESTATIC,
                agentLibClassName,
                "lineNumber",
                "(I)V",
                false
            )
        )

        add(LdcInsnNode(methodName))
        add(
            MethodInsnNode(
                INVOKESTATIC,
                agentLibClassName,
                "methodName",
                "(Ljava/lang/String;)V",
                false
            )
        )
    }

    private fun instrumentVarInsn(
        methodNode: MethodNode,
        varInsnNode: VarInsnNode,
        lineNumber: Int
    ): List<InsnListInsertion> {
        val localVariable = methodNode.localVariables.first { it.index == varInsnNode.`var` }

        return listOf(
            methodNode.instructions.insertBefore(varInsnNode) {
                add(InsnNode(OpcodeUtil.getDupOpcode(varInsnNode.opcode)))
            },
            methodNode.instructions.insertAfter(varInsnNode) {
                recordStore(lineNumber, localVariable)
            }
        )
    }

    private fun instrumentIincInsn(
        methodNode: MethodNode,
        iincInsnNode: IincInsnNode,
        lineNumber: Int
    ): List<InsnListInsertion> {
        val localVariable = methodNode.localVariables.first { it.index == iincInsnNode.`var` }

        return listOf(
            methodNode.instructions.insertAfter(iincInsnNode) {
                add(VarInsnNode(ILOAD, iincInsnNode.`var`))
                recordStore(lineNumber, localVariable)
            }
        )
    }

    /**
     * Records a store into a local variable.
     *
     * @param lineNumber The closest line number of the store.
     * @param localVariable The local variable being stored into.
     */
    private fun InsnList.recordStore(lineNumber: Int, localVariable: LocalVariableNode) =
        recordStore(
            lineNumber,
            localVariable.name,
            TypeUtil.getAdaptedDescriptor(Type.getType(localVariable.desc))
        )

    /**
     * Records a store into a field.
     *
     * @param lineNumber The closest line number of the store.
     * @param field The field being stored into.
     */
    private fun InsnList.recordStore(lineNumber: Int, field: FieldInsnNode) =
        recordStore(
            lineNumber,
            "${convertPathTypeToPackageType(field.owner)}.${field.name}",
            TypeUtil.getAdaptedDescriptor(Type.getType(field.desc))
        )

    /**
     * Records a store into any type of variable.
     *
     * @param lineNumber The closest line number of the store.
     * @param variableName The name of the variable being stored into.
     * @param adaptedVariableDesc The "adapted" type descriptor of the variable. See
     * [TypeUtil.getAdaptedDescriptor].
     */
    private fun InsnList.recordStore(
        lineNumber: Int,
        variableName: String,
        adaptedVariableDesc: String
    ) {
        add(LdcInsnNode(fullyQualifiedClassName))
        add(LdcInsnNode(lineNumber))
        add(LdcInsnNode(variableName))
        add(
            MethodInsnNode(
                INVOKESTATIC,
                agentLibClassName,
                "store",
                "(${adaptedVariableDesc}Ljava/lang/String;ILjava/lang/String;)V",
                false
            )
        )
    }

    /**
     * Determines whether the method is an instance initialization method according to JVMS Section
     * 2.9.1.
     *
     * @return True if the method is an instance initialization method.
     */
    private fun MethodNode.isInstanceInitializationMethod(): Boolean =
        name == "<init>" && Type.getReturnType(desc).sort == Type.VOID

    /**
     * Determines whether the method is an class initialization method according to JVMS Section
     * 2.9.2.
     *
     * @return True if the method is an class initialization method.
     */
    private fun MethodNode.isClassInitializationMethod(): Boolean {
        val majorVersion = classNode.version and 0xFFFF

        val versionCheck = if (majorVersion >= 51) {
            hasAccessFlag(access, ACC_STATIC) &&
                Type.getArgumentTypes(desc).isEmpty()
        } else true

        return name == "<clinit>" &&
            Type.getReturnType(desc).sort == Type.VOID &&
            versionCheck
    }

    /**
     * Determines whether the method is the "main method" (entry point) according to JLS Section
     * 12.1.4.
     *
     * @return True if the method is the "main method".
     */
    private fun MethodNode.isMainMethod(): Boolean {
        val argumentTypes = Type.getArgumentTypes(desc)
        return name == "main" &&
            hasAccessFlag(access, ACC_PUBLIC) &&
            hasAccessFlag(access, ACC_STATIC) &&
            Type.getReturnType(desc).sort == Type.VOID &&
            argumentTypes.size == 1 &&
            argumentTypes[0].descriptor == "[Ljava/lang/String;"
    }

    companion object {

        private val logger = KotlinLogging.logger { }

        /**
         * Checks if an access flag is present. See [Opcodes] for the flags.
         *
         * @param access The access int to check.
         * @param flag The flag to check for.
         * @return True if the flag is present.
         */
        private fun hasAccessFlag(access: Int, flag: Int) = (access and flag) == flag
    }
}

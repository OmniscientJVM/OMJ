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
import com.octogonapus.omj.di.OMJKoinComponent
import mu.KotlinLogging
import org.koin.core.inject
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.Opcodes.PUTFIELD
import org.objectweb.asm.Opcodes.PUTSTATIC
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.IincInsnNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.VarInsnNode

internal class OMJClassTransformer(
    private val classNode: ClassNode
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
        when {
            methodNode.isInstanceInitializationMethod() ->
                instrumentInstanceInitializationMethod(methodNode)

            methodNode.isClassInitializationMethod() ->
                instrumentClassInitializationMethod(methodNode)

            methodNode.isMainMethod() -> instrumentMainMethod(methodNode)

            else -> instrumentNormalMethod(methodNode)
        }
    }

    private fun instrumentInstanceInitializationMethod(methodNode: MethodNode) {
        TODO("Not yet implemented")
    }

    private fun instrumentClassInitializationMethod(methodNode: MethodNode) {
        TODO("Not yet implemented")
    }

    private fun instrumentMainMethod(methodNode: MethodNode) {
        TODO("Not yet implemented")
    }

    private fun instrumentNormalMethod(methodNode: MethodNode) {
        var currentLineNumber = LineNumberNode(0, LabelNode())

        methodNode.instructions.flatMap {
            when (it) {
                is LineNumberNode -> {
                    currentLineNumber = it
                    emptyList()
                }

                is MethodInsnNode -> if (classFilter.shouldTransform(it.owner)) {
                    TODO("Emit preamble")
                } else emptyList()

                is VarInsnNode -> when (it.opcode) {
                    ISTORE, LSTORE, FSTORE, DSTORE, ASTORE ->
                        instrumentVarInsn(methodNode, it, currentLineNumber.line)
                    else -> emptyList()
                }

                is IincInsnNode -> TODO("Record IINC")

                is FieldInsnNode -> when (it.opcode) {
                    PUTFIELD, PUTSTATIC -> TODO("Record store")
                    else -> emptyList()
                }

                else -> emptyList()
            }
        }.forEach { insertion ->
            insertion.insert()
        }
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
                add(LdcInsnNode(fullyQualifiedClassName))
                add(LdcInsnNode(lineNumber))
                add(LdcInsnNode(localVariable.name))
                add(
                    MethodInsnNode(
                        INVOKESTATIC,
                        agentLibClassName,
                        "store",
                        "(${localVariable.desc}Ljava/lang/String;ILjava/lang/String;)V",
                        false
                    )
                )
            }
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

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

import com.octogonapus.omj.di.OMJKoinComponent
import com.octogonapus.omj.util.nullableSingleAssign
import com.octogonapus.omj.util.singleAssign
import mu.KotlinLogging
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class OMJClassAdapter(
    api: Int,
    classVisitor: ClassVisitor?
) : ClassVisitor(api, classVisitor), OMJKoinComponent {

    private var classVersion = 0
    private var className by singleAssign<String>()
    private var superName by nullableSingleAssign<String>()

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        logger.debug {
            """
            version = $version
            access = $access
            name = $name
            signature = $signature
            superName = $superName
            interfaces = ${interfaces?.contentDeepToString()}
            """.trimIndent()
        }

        classVersion = version
        className = name
        this.superName = superName

        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor {
        logger.debug {
            """
            access = $access
            name = $name
            descriptor = $descriptor
            signature = $signature
            exceptions = ${exceptions?.contentDeepToString()}
            """.trimIndent()
        }

        val visitor = super.visitMethod(access, name, descriptor, signature, exceptions)

        // If superName is null, we are visiting the Object class, so there is nothing for us to
        // instrument. Otherwise, determine which adapter to use.
        return superName?.let { superName ->
            determineMethodAdapter(name, descriptor, visitor, superName, access)
        } ?: visitor
    }

    private fun determineMethodAdapter(
        name: String,
        descriptor: String,
        visitor: MethodVisitor,
        superName: String,
        access: Int
    ): MethodVisitor {
        val method = Method(name, descriptor)

        return when {
            isInstanceInitializationMethod(method) ->
                OMJInstanceInitializationMethodAdapter(
                    api,
                    visitor,
                    method,
                    className,
                    superName
                )

            isClassInitializationMethod(method, access) -> OMJMethodAdapter(
                api,
                visitor,
                method,
                hasAccessFlag(access, Opcodes.ACC_STATIC),
                className
            )

            isMainMethod(method, access) -> OMJMainMethodAdapter(api, visitor, method, className)

            else -> OMJMethodAdapter(
                api,
                visitor,
                method,
                hasAccessFlag(access, Opcodes.ACC_STATIC),
                className
            )
        }
    }

    /**
     * Determines whether the method is the "main method" (entry point) according to JLS Section
     * 12.1.4.
     *
     * @param method The method to check.
     * @param access The method's access number.
     * @return True if the method is the "main method".
     */
    private fun isMainMethod(method: Method, access: Int): Boolean {
        val argumentTypes = Type.getArgumentTypes(method.descriptor)
        return (
            method.name == "main" && hasAccessFlag(access, Opcodes.ACC_PUBLIC) &&
                hasAccessFlag(access, Opcodes.ACC_STATIC) &&
                Type.getReturnType(method.descriptor).sort == Type.VOID &&
                argumentTypes.size == 1 &&
                argumentTypes[0].descriptor == "[Ljava/lang/String;"
            )
    }

    /**
     * Determines whether the method is an instance initialization method according to JVMS Section
     * 2.9.1.
     *
     * @param method The method to check.
     * @return True if the method is an instance initialization method.
     */
    private fun isInstanceInitializationMethod(method: Method) =
        method.name == "<init>" && Type.getReturnType(method.descriptor).sort == Type.VOID

    /**
     * Determines whether the method is an class initialization method according to JVMS Section
     * 2.9.2.
     *
     * @param method The method to check.
     * @return True if the method is an class initialization method.
     */
    private fun isClassInitializationMethod(method: Method, access: Int): Boolean {
        val majorVersion = classVersion and 0xFFFF

        val versionCheck = if (majorVersion >= 51) {
            hasAccessFlag(access, Opcodes.ACC_STATIC) &&
                Type.getArgumentTypes(method.descriptor).isEmpty()
        } else {
            true
        }

        return method.name == "<clinit>" &&
            Type.getReturnType(method.descriptor).sort == Type.VOID &&
            versionCheck
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
        private fun hasAccessFlag(access: Int, flag: Int) = access and flag == flag
    }
}

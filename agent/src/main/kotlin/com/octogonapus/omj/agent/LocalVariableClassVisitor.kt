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
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor

internal class LocalVariableClassVisitor(
    api: Int,
    classVisitor: ClassVisitor?
) : ClassVisitor(api, classVisitor) {

    val localVariableVisitors = mutableMapOf<Method, LocalVariableMethodVisitor>()

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val visitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        val localVariableMethodVisitor = LocalVariableMethodVisitor(api, visitor)
        val method = Method(name, descriptor)
        localVariableVisitors[method] = localVariableMethodVisitor
        logger.debug {
            """
            Visited method
            access = $access
            name = $name
            """.trimIndent()
        }
        return localVariableMethodVisitor
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}

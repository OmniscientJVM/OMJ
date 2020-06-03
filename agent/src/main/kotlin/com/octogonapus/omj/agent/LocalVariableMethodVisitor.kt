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
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type

internal class LocalVariableMethodVisitor(
    api: Int,
    superVisitor: MethodVisitor?
) : MethodVisitor(api, superVisitor) {

    val locals = mutableListOf<LocalVariable>()

    override fun visitLocalVariable(
        name: String,
        descriptor: String,
        signature: String?,
        start: Label,
        end: Label,
        index: Int
    ) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index)

        // Adapt the descriptor for consumers that don't care about what type of reference (if it
        // is a reference).
        val adaptedDescriptor = TypeUtil.getAdaptedDescriptor(Type.getType(descriptor))
        locals.add(LocalVariable(name, descriptor, adaptedDescriptor, index))

        logger.debug {
            """
            Visited local variable
            name = $name
            descriptor = $descriptor
            adaptedDescriptor = $adaptedDescriptor
            index = $index
            """.trimIndent()
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}

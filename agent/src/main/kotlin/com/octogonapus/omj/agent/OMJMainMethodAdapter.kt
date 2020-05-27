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

import com.octogonapus.omj.agent.MethodAdapterUtil.convertPathTypeToPackageType
import com.octogonapus.omj.agent.MethodAdapterUtil.recordMethodTrace
import com.octogonapus.omj.agent.MethodAdapterUtil.visitMethodCallStartPreamble
import mu.KotlinLogging
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

internal class OMJMainMethodAdapter(
    api: Int,
    private val superVisitor: MethodVisitor,
    private val dynamicClassDefiner: DynamicClassDefiner,
    private val classFilter: ClassFilter,
    currentClassName: String
) : MethodVisitor(api, superVisitor), Opcodes {

    private val fullyQualifiedClassName = convertPathTypeToPackageType(currentClassName)
    private var currentLineNumber = 0

    override fun visitCode() {
        super.visitCode()
        // Hardcode the main method name because otherwise we usually won't emit a preamble for it
        // because it's usually called by the JVM, not by user code. In the case that a user does
        // call main themselves, then this preamble is redundant.
        superVisitor.visitMethodCallStartPreamble(
            currentLineNumber,
            fullyQualifiedClassName,
            "main"
        )
        superVisitor.recordMethodTrace("([Ljava/lang/String;)V", true, dynamicClassDefiner, logger)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        logger.debug {
            """
            opcode = $opcode
            owner = $owner
            name = $name
            descriptor = $descriptor
            isInterface = $isInterface
            """.trimIndent()
        }

        // Only add the preamble to methods which we will also record a trace for
        if (classFilter.shouldTransform(owner)) {
            superVisitor.visitMethodCallStartPreamble(
                currentLineNumber,
                fullyQualifiedClassName,
                name
            )
        }

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
    }

    override fun visitLineNumber(line: Int, start: Label) {
        super.visitLineNumber(line, start)
        currentLineNumber = line
    }

    companion object {

        private val logger = KotlinLogging.logger { }
    }
}
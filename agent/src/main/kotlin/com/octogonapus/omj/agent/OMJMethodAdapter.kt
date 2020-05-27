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

import com.octogonapus.omj.agent.MethodAdapterUtil.recordMethodTrace
import com.octogonapus.omj.agent.MethodAdapterUtil.visitMethodCallStartPreamble
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.slf4j.LoggerFactory

internal class OMJMethodAdapter(
    api: Int,
    private val superVisitor: MethodVisitor,
    private val dynamicClassDefiner: DynamicClassDefiner,
    private val classFilter: ClassFilter,
    private val methodDescriptor: String,
    private val isStatic: Boolean,
    currentClassName: String
) : MethodVisitor(api, superVisitor), Opcodes {

    private val logger = LoggerFactory.getLogger(OMJMethodAdapter::class.java)
    private val fullyQualifiedClassName: String
    private var currentLineNumber = 0

    init {
        val indexOfLastSeparator = currentClassName.lastIndexOf('/') + 1
        val packagePrefix = currentClassName.substring(0, indexOfLastSeparator)
        val className = currentClassName.substring(indexOfLastSeparator)
        fullyQualifiedClassName = packagePrefix.replace('/', '.') + className
    }

    override fun visitCode() {
        super.visitCode()
        superVisitor.recordMethodTrace(methodDescriptor, isStatic, dynamicClassDefiner, logger)
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
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
}

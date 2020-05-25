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

internal class OMJInstanceInitializationMethodAdapter(
    api: Int,
    private val superVisitor: MethodVisitor,
    private val dynamicClassDefiner: DynamicClassDefiner,
    private val methodDescriptor: String,
    currentClassName: String,
    private val superName: String
) : MethodVisitor(api, superVisitor), Opcodes {

    private val logger = LoggerFactory.getLogger(OMJInstanceInitializationMethodAdapter::class.java)
    private val fullyQualifiedClassName: String
    private var currentLineNumber = 0

    init {
        val indexOfLastSeparator = currentClassName.lastIndexOf('/') + 1
        val packagePrefix = currentClassName.substring(0, indexOfLastSeparator)
        val className = currentClassName.substring(indexOfLastSeparator)
        fullyQualifiedClassName = packagePrefix.replace('/', '.') + className
    }

    override fun visitMethodInsn(
        opcode: Int,
        owner: String,
        name: String,
        descriptor: String,
        isInterface: Boolean
    ) {
        logger.debug(
                "visitMethodInsn opcode = {}, owner = {}, name = {}, descriptor = {}, " +
                        "isInterface = {}",
                opcode,
                owner,
                name,
                descriptor,
                isInterface)

        superVisitor.visitMethodCallStartPreamble(currentLineNumber, fullyQualifiedClassName, name)
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        if (opcode == Opcodes.INVOKESPECIAL && name == "<init>" && owner == superName) {
            // This is the superclass instance initialization method. We can't access `this` until after
            // the superclass is initialized, so recording this method trace has to go in here right after
            // the superclass is initialized instead of in `visitCode`, which would put it at the start of
            // this method before the superclass is initialized.
            superVisitor.recordMethodTrace(methodDescriptor, false, dynamicClassDefiner, logger)
        }
    }

    override fun visitLineNumber(line: Int, start: Label) {
        super.visitLineNumber(line, start)
        currentLineNumber = line
    }
}

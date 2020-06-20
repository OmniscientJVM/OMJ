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
import java.io.PrintWriter
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain
import kotlin.system.exitProcess
import mu.KotlinLogging
import org.koin.core.inject
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ASM8
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.util.TraceClassVisitor

internal class OMJClassFileTransformer(
    private val transformer: Transformer = Transformer()
) : ClassFileTransformer, OMJKoinComponent {

    private val classFilter by inject<ClassFilter>()

    // We need to catch any exception during transformation so that we can be loud about it
    @Suppress("TooGenericExceptionCaught")
    override fun transform(
        loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>?,
        protectionDomain: ProtectionDomain,
        classfileBuffer: ByteArray
    ): ByteArray? {
        // Check the include and exclude class filters as to whether we should transform this
        // class. We need to check this here so that we can return `null` to obey `transform`'s
        // contract.
        return if (classFilter.shouldTransform(className)) {
            try {
                // If transformClassBytes throws an exception, then the class will silently not
                // be transformed. This is very hard to debug, so catch anything it throws and
                // explode.
                transformer.transformClassBytes(classfileBuffer)
            } catch (ex: Throwable) {
                logger.error(ex) {
                    """
                    Failed to transform class bytes.
                    Bytes: ${classfileBuffer.contentToString()}
                    """.trimIndent()
                }
                exitProcess(1)
            }
        } else {
            // Return `null` if we won't transform this class to obey the contract.
            null
        }
    }

    /**
     * Transforms the class byte array. This method is pulled into another class so that it can be
     * mocked for testing.
     */
    internal class Transformer {

        internal fun transformClassBytes(classfileBuffer: ByteArray): ByteArray {
            val classReader = ClassReader(classfileBuffer)

            val classNode = ClassNode(ASM8)
            classReader.accept(classNode, 0)

            val transformer = OMJClassTransformer(classNode)
            transformer.transform()

            val classWriter = ClassWriter(ClassWriter.COMPUTE_FRAMES)

            // Add a `TraceClassVisitor` to print the generated bytecode to System.out for
            // debugging.
            val trace = TraceClassVisitor(classWriter, PrintWriter(System.out))

            classNode.accept(trace)

            return classWriter.toByteArray()
        }
    }

    companion object {

        private val logger = KotlinLogging.logger { }
    }
}

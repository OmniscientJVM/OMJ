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
package com.octogonapus.omj.ui.model

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class TraceIteratorTest {

    @Test
    fun `parse method call with no args`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_noargs.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.noargs.Foo") },
                { it.virtualMethodCall("com.agenttest.noargs.Foo") }
        )
    }

    @Test
    fun `parse method call with args byte 3C`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_byte3c.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.byte3c.Foo") },
                { it.virtualMethodCall("com.agenttest.byte3c.Foo", "byte" to "60") }
        )
    }

    @Test
    fun `parse method call with args char Q`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_charQ.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.charQ.Foo") },
                { it.virtualMethodCall("com.agenttest.charQ.Foo", "char" to "Q") }
        )
    }

    @Test
    fun `parse method call with args double 1p2`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_double1p2.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.double1p2.Foo") },
                { it.virtualMethodCall("com.agenttest.double1p2.Foo", "double" to "1.2") }
        )
    }

    @Test
    fun `parse method call with args float 4p3`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_float4p3.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.float4p3.Foo") },
                { it.virtualMethodCall("com.agenttest.float4p3.Foo", "float" to "4.3") }
        )
    }

    @Test
    fun `parse method call with args int 42`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_int42.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.int42.Foo") },
                { it.virtualMethodCall("com.agenttest.int42.Foo", "int" to "42") }
        )
    }

    @Test
    fun `parse method call with args long 123456789123456789`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_long123456789123456789.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.long123456789123456789.Foo") },
                {
                    it.virtualMethodCall(
                            "com.agenttest.long123456789123456789.Foo",
                            "long" to "123456789123456789"
                    )
                }
        )
    }

    @Test
    fun `parse method call with args string hello`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_stringHello.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.stringHello.Foo") },
                {
                    it.virtualMethodCall(
                            "com.agenttest.stringHello.Foo",
                            "java.lang.String" to "Hello"
                    )
                }
        )
    }

    @Test
    fun `parse method call with args string hello with null byte`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_stringHelloNull1.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.stringHelloNull1.Foo") },
                {
                    it.virtualMethodCall("com.agenttest.stringHelloNull1.Foo",
                            "java.lang.String" to "Hello\u0000 1")
                }
        )
    }

    @Test
    fun `parse method call with args object`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_objectStringArray.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.objectStringArray.Foo") },
                { it.virtualMethodCall(
                        "com.agenttest.objectStringArray.Foo",
                        "[Ljava/lang/String;" to null
                ) }
        )
    }

    @Test
    fun `parse method call with args short 12345`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_short12345.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.short12345.Foo") },
                { it.virtualMethodCall("com.agenttest.short12345.Foo", "short" to "12345") }
        )
    }

    @Test
    fun `parse method call with args boolean true`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_booleanTrue.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.booleanTrue.Foo") },
                { it.virtualMethodCall("com.agenttest.booleanTrue.Foo", "boolean" to "true") }
        )
    }

    @Test
    fun `parse method call with args object MyDataClass`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_objectTestDataClass.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.objectTestDataClass.Foo") },
                {
                    it.virtualMethodCall(
                            "com.agenttest.objectTestDataClass.Foo",
                            "com.agenttest.objectTestDataClass.TestDataClass" to null
                    )
                }
        )
    }

    @Test
    fun `parse constructor call with int 6`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_constructorInt6.jar")

        traces.shouldHaveInOrder(
                { it.constructorCall("com.agenttest.constructorInt6.Foo", "int" to "6") }
        )
    }

    @Test
    fun `read past end of trace`(@TempDir tempDir: File) {
        runAgent("agent-test_noargs.jar", tempDir.toPath())

        val traceFiles = tempDir.listFiles()!!.toList().filter { it.extension == "trace" }
        traceFiles.shouldHaveSize(1)

        TraceIterator(BufferedInputStream(FileInputStream(traceFiles[0]))).use {
            // Go to the end
            while (it.hasNext()) {
                it.next()
            }

            // Past the end
            shouldThrow<NoSuchElementException> { it.next() }
        }
    }

    companion object {

        /**
         * Generate traces by running the Jar under the agent. Asserts that there is only one trace
         * file.
         *
         * @param tempDir The dir to save the trace file into.
         * @param jarFilename The filename of the Jar to load from
         * `rootProject/build/agent-test-jars`.
         * @return The traces.
         */
        private fun generateTraces(tempDir: File, jarFilename: String): List<Trace> {
            runAgent(jarFilename, tempDir.toPath())

            val traceFiles = tempDir.listFiles()!!.filter { it.extension == "trace" }
            traceFiles.shouldHaveSize(1)

            return TraceIterator(BufferedInputStream(FileInputStream(traceFiles[0]))).use {
                it.asSequence().toList()
            }
        }

        /**
         * Assumes there is a virtual method call and asserts about its receiver type and arguments.
         * Excludes instance initialization methods.
         *
         * @param receiverType The expected receiver type.
         * @param args The expected (type, value) pairs for each argument in order.
         */
        private fun Trace.virtualMethodCall(
            receiverType: String,
            vararg args: Pair<String, String?>
        ) = this is MethodTrace && !isStatic && hasArgumentType(0, receiverType) &&
                methodName != "<init>" && hasArguments(args)

        private fun Trace.constructorCall(
            receiverType: String,
            vararg args: Pair<String, String?>
        ) = this is MethodTrace && !isStatic && hasArgumentType(0, receiverType) &&
                methodName == "<init>" && hasArguments(args)

        private fun MethodTrace.hasArguments(args: Array<out Pair<String, String?>>): Boolean {
            return args.iterator().asSequence().foldIndexed(true) { index, acc, (type, value) ->
                // Skip index zero because that is the receiver index, which we already checked
                if (index == 0) true
                else {
                    if (value == null) {
                        // Null means we don't care about the value
                        acc && hasArgumentType(index, type)
                    } else {
                        acc && hasArgument(index, type, value)
                    }
                }
            }
        }

        private fun MethodTrace.hasArgumentType(index: Int, type: String) =
                arguments[index].type == type

        private fun MethodTrace.hasArgument(index: Int, type: String, value: String) =
                arguments[index].type == type && arguments[index].value == value
    }
}

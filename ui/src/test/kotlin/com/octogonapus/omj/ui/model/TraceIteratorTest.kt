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
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class TraceIteratorTest {

    @Test
    fun `parse method call with no args`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_noargs.jar")

        traces.shouldHaveSingleElement {
            it is MethodTrace && it.virtualMethodCall("com.agenttest.noargs.Foo")
        }
    }

    @Test
    fun `parse method call with args byte 3C`(@TempDir tempDir: File) {
        val traces = generateTraces(tempDir, "agent-test_byte3c.jar")

        traces.shouldHaveSingleElement {
            it is MethodTrace && it.virtualMethodCall("com.agenttest.byte3c.Foo", "byte" to "60")
        }
    }

    @Test
    fun `parse method call with args char Q`() {
        val traces = loadTraces("method_call_args_char_Q.trace")
        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            it is MethodTrace &&
                    it.arguments.size == 2 &&
                    it.hasArgumentType(0, "com.octogonapus.PrintHello") &&
                    it.hasArgument(1, "char", "Q")
        }
    }

    @Test
    fun `parse method call with args double 1p2`() {
        val traces = loadTraces("method_call_args_double_1p2.trace")
        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            it is MethodTrace &&
                    it.arguments.size == 2 &&
                    it.hasArgumentType(0, "com.octogonapus.PrintHello") &&
                    it.hasArgument(1, "double", "1.2")
        }
    }

    @Test
    fun `parse method call with args float 4p3`() {
        val traces = loadTraces("method_call_args_float_4p3.trace")
        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            it is MethodTrace &&
                    it.arguments.size == 2 &&
                    it.hasArgumentType(0, "com.octogonapus.PrintHello") &&
                    it.hasArgument(1, "float", "4.3")
        }
    }

    @Test
    fun `parse method call with args int 42`() {
        val traces = loadTraces("method_call_args_int_42.trace")
        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            it is MethodTrace &&
                    it.arguments.size == 2 &&
                    it.hasArgumentType(0, "com.octogonapus.PrintHello") &&
                    it.hasArgument(1, "int", "42")
        }
    }

    @Test
    fun `parse method call with args long 123456789123456789`() {
        val traces = loadTraces("method_call_args_long_123456789123456789.trace")
        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            it is MethodTrace &&
                    it.arguments.size == 2 &&
                    it.hasArgumentType(0, "com.octogonapus.PrintHello") &&
                    it.hasArgument(1, "long", "123456789123456789")
        }
    }

    @Test
    fun `parse method call with args string hello`() {
        val traces = loadTraces("method_call_args_string_hello.trace")
        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            it is MethodTrace &&
                    it.arguments.size == 2 &&
                    it.hasArgumentType(0, "com.octogonapus.PrintHello") &&
                    it.hasArgument(1, "java.lang.String", "Hello")
        }
    }

    @Test
    fun `parse method call with args string hello with null byte`() {
        val traces = loadTraces("method_call_args_string_helloWithNullByte.trace")
        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            it is MethodTrace &&
                    it.arguments.size == 2 &&
                    it.hasArgumentType(0, "com.octogonapus.PrintHello") &&
                    it.hasArgument(1, "java.lang.String", "Hello\u0000 1")
        }
    }

    @Test
    fun `parse method call with args object`() {
        val traces = loadTraces("method_call_args_object.trace")
        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            // TODO: Parse the descriptor and turn it into a more human-readable notation like
            //  java.lang.String[]
            it is MethodTrace &&
                    it.arguments.size == 2 &&
                    it.hasArgumentType(0, "com.octogonapus.PrintHello") &&
                    it.hasArgumentType(1, "[Ljava.lang.String;")
        }
    }

    @Test
    fun `parse method call with args short 12345`() {
        val traces = loadTraces("method_call_args_short_12345.trace")
        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            it is MethodTrace &&
                    it.arguments.size == 2 &&
                    it.hasArgumentType(0, "com.octogonapus.PrintHello") &&
                    it.hasArgument(1, "short", "12345")
        }
    }

    @Test
    fun `parse method call with args boolean true`() {
        val traces = loadTraces("method_call_args_boolean_true.trace")
        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            it is MethodTrace &&
                    it.arguments.size == 2 &&
                    it.hasArgumentType(0, "com.octogonapus.PrintHello") &&
                    it.hasArgument(1, "boolean", "true")
        }
    }

    @Test
    fun `parse method call with args object MyDataClass`() {
        val traces = loadTraces("method_call_args_object_MyDataClass.trace")
        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            it is MethodTrace &&
                    it.arguments.size == 2 &&
                    it.hasArgumentType(0, "com.octogonapus.PrintHello") &&
                    it.hasArgumentType(1, "com.octogonapus.MyDataClass")
        }
    }

    @Test
    fun `read past end of trace`() {
        getIter("method_call_no_args.trace").use {
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

            val traceFiles = tempDir.listFiles()!!.toList()
            traceFiles.shouldHaveSize(1)

            return TraceIterator(BufferedInputStream(FileInputStream(traceFiles[0]))).use {
                it.asSequence().toList()
            }
        }

        private fun traceIndexShouldMatchListIndex(traces: List<Trace>) {
            traces.forEachIndexed { index, trace ->
                trace.shouldBeInstanceOf<MethodTrace> {
                    it.index.shouldBe(index)
                }
            }
        }

        /**
         * Assumes there is a virtual method call and asserts about its receiver type and arguments.
         *
         * @param receiverType The expected receiver type.
         * @param args The expected (type, value) pairs for each argument in order.
         */
        private fun MethodTrace.virtualMethodCall(
            receiverType: String,
            vararg args: Pair<String, String>
        ) = hasArgumentType(0, receiverType) &&
                args.iterator().asSequence().foldIndexed(true) { index, acc, (type, value) ->
                    // Skip index zero because that is the receiver index, which we already checked
                    if (index == 0) true
                    else acc && hasArgument(index, type, value)
                }

        private fun MethodTrace.hasArgumentType(index: Int, type: String) =
                arguments[index].type == type

        private fun MethodTrace.hasArgument(index: Int, type: String, value: String) =
                arguments[index].type == type && arguments[index].value == value

        private fun loadTraces(name: String): List<Trace> =
                getIter(name).use { it.asSequence().toList() }

        private fun getIter(name: String) =
                TraceIterator(BufferedInputStream(FileInputStream(
                        Paths.get(TraceIteratorTest::class.java.getResource(name).toURI()).toFile()
                )))
    }
}

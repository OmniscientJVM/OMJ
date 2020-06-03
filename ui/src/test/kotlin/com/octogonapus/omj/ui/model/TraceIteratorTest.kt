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

import com.octogonapus.omj.testutil.CompileUtil
import com.octogonapus.omj.testutil.shouldHaveInOrder
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.collections.shouldHaveSize
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class TraceIteratorTest {

    @Nested
    inner class MethodTraceTests {

        @Test
        fun `parse method call with no args`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_noargs.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.noargs.Foo",
                        callerClass = "com.agenttest.noargs.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.noargs.Foo",
                        callerClass = "com.agenttest.noargs.Main",
                        methodName = "with"
                    )
                }
            )
        }

        @Test
        fun `parse method call with args byte 3C`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_byte3c.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.byte3c.Foo",
                        callerClass = "com.agenttest.byte3c.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.byte3c.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.byte3c.Main",
                        args = listOf("byte" to "60")
                    )
                }
            )
        }

        @Test
        fun `parse method call with args char Q`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_charQ.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.charQ.Foo",
                        callerClass = "com.agenttest.charQ.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.charQ.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.charQ.Main",
                        args = listOf("char" to "Q")
                    )
                }
            )
        }

        @Test
        fun `parse method call with args double 1p2`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_double1p2.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.double1p2.Foo",
                        callerClass = "com.agenttest.double1p2.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.double1p2.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.double1p2.Main",
                        args = listOf("double" to "1.2")
                    )
                }
            )
        }

        @Test
        fun `parse method call with args float 4p3`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_float4p3.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.float4p3.Foo",
                        callerClass = "com.agenttest.float4p3.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.float4p3.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.float4p3.Main",
                        args = listOf("float" to "4.3")
                    )
                }
            )
        }

        @Test
        fun `parse method call with args int 42`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_int42.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.int42.Foo",
                        callerClass = "com.agenttest.int42.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.int42.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.int42.Main",
                        args = listOf("int" to "42")
                    )
                }
            )
        }

        @Test
        fun `parse method call with args long 123456789123456789`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_long123456789123456789.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.long123456789123456789.Foo",
                        callerClass = "com.agenttest.long123456789123456789.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.long123456789123456789.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.long123456789123456789.Main",
                        args = listOf("long" to "123456789123456789")
                    )
                }
            )
        }

        @Test
        fun `parse method call with args string hello`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_stringHello.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.stringHello.Foo",
                        callerClass = "com.agenttest.stringHello.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.stringHello.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.stringHello.Main",
                        args = listOf("java.lang.String" to "Hello")
                    )
                }
            )
        }

        @Test
        fun `parse method call with args string hello with null byte`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_stringHelloNull1.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.stringHelloNull1.Foo",
                        callerClass = "com.agenttest.stringHelloNull1.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.stringHelloNull1.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.stringHelloNull1.Main",
                        args = listOf("java.lang.String" to "Hello\u0000 1")
                    )
                }
            )
        }

        @Test
        fun `parse method call with args object`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_objectStringArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.objectStringArray.Foo",
                        callerClass = "com.agenttest.objectStringArray.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.objectStringArray.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.objectStringArray.Main",
                        args = listOf("[Ljava.lang.String;" to null)
                    )
                }
            )
        }

        @Test
        fun `parse method call with args short 12345`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_short12345.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.short12345.Foo",
                        callerClass = "com.agenttest.short12345.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.short12345.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.short12345.Main",
                        args = listOf("short" to "12345")
                    )
                }
            )
        }

        @Test
        fun `parse method call with args boolean true`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_booleanTrue.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.booleanTrue.Foo",
                        callerClass = "com.agenttest.booleanTrue.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.booleanTrue.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.booleanTrue.Main",
                        args = listOf("boolean" to "true")
                    )
                }
            )
        }

        @Test
        fun `parse method call with args object MyDataClass`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_objectTestDataClass.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.objectTestDataClass.Foo",
                        callerClass = "com.agenttest.objectTestDataClass.Main"
                    )
                },
                {
                    it.virtualMethodCall(
                        receiverType = "com.agenttest.objectTestDataClass.Foo",
                        methodName = "with",
                        callerClass = "com.agenttest.objectTestDataClass.Main",
                        args = listOf("com.agenttest.objectTestDataClass.TestDataClass" to null)
                    )
                }
            )
        }

        @Test
        fun `parse constructor call with int 6`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_constructorInt6.jar")

            traces.shouldHaveInOrder(
                {
                    it.constructorCall(
                        receiverType = "com.agenttest.constructorInt6.Foo",
                        callerClass = "com.agenttest.constructorInt6.Main",
                        args = listOf("int" to "6")
                    )
                }
            )
        }

        @Test
        fun `parse a static method call in a static block`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_staticBlockCallStaticMethod.jar")

            traces.shouldHaveInOrder(
                {
                    it.staticMethodCall(
                        methodName = "callMe",
                        callerClass = "com.agenttest.staticBlockCallStaticMethod.Foo"
                    )
                }
            )
        }

        @Test
        fun `parse a static method call with an int after a double`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_methodCallIntAfterDouble.jar")

            traces.shouldHaveInOrder(
                {
                    it.staticMethodCall(
                        methodName = "callMe",
                        callerClass = "com.agenttest.methodCallIntAfterDouble.Main",
                        args = listOf("double" to "4.2", "int" to "1")
                    )
                }
            )
        }
    }

    @Nested
    inner class LocalVariableStoreTraceTests {

        @Test
        fun `test boolean store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeBoolean.jar")

            traces.shouldExist {
                it.storeVar("com.agenttest.storeBoolean.Main", "boolean", "b", "true")
            }
        }

        @Test
        fun `test byte store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeByte.jar")

            traces.shouldExist {
                it.storeVar("com.agenttest.storeByte.Main", "byte", "b", "250")
            }
        }

        @Test
        fun `test char store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeChar.jar")

            traces.shouldExist {
                it.storeVar("com.agenttest.storeChar.Main", "char", "c", "Q")
            }
        }

        @Test
        fun `test double store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeDouble.jar")

            traces.shouldExist {
                it.storeVar("com.agenttest.storeDouble.Main", "double", "d", "4.2")
            }
        }

        @Test
        fun `test float store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeFloat.jar")

            traces.shouldExist {
                it.storeVar("com.agenttest.storeFloat.Main", "float", "f", "2.3")
            }
        }

        @Test
        fun `test int store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeInt.jar")

            traces.shouldExist {
                it.storeVar("com.agenttest.storeInt.Main", "int", "i", "123456")
            }
        }

        @Test
        fun `test long store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeLong.jar")

            traces.shouldExist {
                it.storeVar("com.agenttest.storeLong.Main", "long", "l", "123456789123456789")
            }
        }

        @Test
        fun `test ref store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeRef.jar")

            traces.shouldExist {
                it.storeVar("com.agenttest.storeRef.Main", "java.lang.Object", "o", null)
            }
        }

        @Test
        fun `test short store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeShort.jar")

            traces.shouldExist {
                it.storeVar("com.agenttest.storeShort.Main", "short", "s", "12345")
            }
        }

        @Test
        fun `test string store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeString.jar")

            traces.shouldExist {
                it.storeVar("com.agenttest.storeString.Main", "java.lang.String", "s", "My String")
            }
        }

        @Test
        fun `test int increment`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeIncrementInt.jar")

            traces.shouldExist {
                // Started at 3 and was incremented to 4
                it.storeVar("com.agenttest.storeIncrementInt.Main", "int", "i", "4")
            }
        }
    }

    @Nested
    inner class FormalMethodParameterStoreTraceTests {

        @Test
        fun `mutate both method params`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeTwoMethodParams.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.storeTwoMethodParams.Main",
                    "java.lang.String",
                    "s",
                    "Second"
                )
                it.storeVar("com.agenttest.storeTwoMethodParams.Main", "int", "i", "2")
            }
        }
    }

    @Test
    fun `read past end of trace`(@TempDir tempDir: File) {
        CompileUtil.checkForAgentTestErrors(
            CompileUtil.runAgentTest("agent-test_noargs.jar", tempDir.toPath())
        )

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
            CompileUtil.checkForAgentTestErrors(
                CompileUtil.runAgentTest(jarFilename, tempDir.toPath())
            )

            val traceFiles = tempDir.listFiles()!!.filter { it.extension == "trace" }
            traceFiles.shouldHaveSize(1)

            return TraceIterator(BufferedInputStream(FileInputStream(traceFiles[0]))).use {
                it.asSequence().toList()
            }
        }

        /**
         * Assumes there is a virtual method call and asserts about its receiver type and arguments.
         * Excludes instance and class initialization methods.
         *
         * @param receiverType The expected receiver type.
         * @param methodName The expected method name.
         * @param callerClass The class that the method is expected to be called from.
         * @param args The expected (type, value) pairs for each argument in order.
         */
        private fun Trace.virtualMethodCall(
            receiverType: String,
            methodName: String,
            callerClass: String,
            args: List<Pair<String, String?>> = emptyList()
        ) = this is MethodTrace &&
            !isStatic &&
            methodName != "<init>" &&
            methodName != "<clinit>" &&
            hasArguments(listOf(receiverType to null) + args) &&
            this.methodName == methodName &&
            this.callerClass == callerClass

        /**
         * Assumes there is a static method call and asserts about its arguments. Excludes instance
         * and class initialization methods.
         *
         * @param methodName The expected method name.
         * @param callerClass The class that the method is expected to be called from.
         * @param args The expected (type, value) pairs for each argument in order.
         */
        private fun Trace.staticMethodCall(
            methodName: String,
            callerClass: String,
            args: List<Pair<String, String?>> = emptyList()
        ) = this is MethodTrace &&
            isStatic &&
            methodName != "<init>" &&
            methodName != "<clinit>" &&
            hasArguments(args) &&
            this.methodName == methodName &&
            this.callerClass == callerClass

        /**
         * Assumes there is an instance initializer method call and asserts about its arguments.
         *
         * @param receiverType The expected receiver type.
         * @param callerClass The class that the method is expected to be called from.
         * @param args The expected (type, value) pairs for each argument in order.
         */
        private fun Trace.constructorCall(
            receiverType: String,
            callerClass: String,
            args: List<Pair<String, String?>> = emptyList()
        ) = this is MethodTrace &&
            !isStatic &&
            methodName == "<init>" &&
            hasArguments(listOf(receiverType to null) + args) &&
            this.callerClass == callerClass

        /**
         * Checks there is a store with a value of [value] into a variable of type [varType].
         *
         * @param containingClass The class the store happens in.
         * @param varType The type of the variable the value was stored in.
         * @param name The name of the variable.
         * @param value The value that was stored. Set to null if you don't care about the value.
         */
        private fun Trace.storeVar(
            containingClass: String,
            varType: String,
            name: String,
            value: String?
        ) =
            this is StoreTrace &&
                callerClass == containingClass &&
                typeValuePair.type == varType &&
                variableName == name &&
                value?.let { typeValuePair.value == it } ?: true

        private fun MethodTrace.hasArguments(args: List<Pair<String, String?>>) =
            args.foldIndexed(true) { index, acc, (type, value) ->
                if (value == null) {
                    // Null means we don't care about the value
                    acc && hasArgumentType(index, type)
                } else {
                    acc && hasArgument(index, type, value)
                }
            }

        private fun MethodTrace.hasArgumentType(index: Int, type: String) =
            arguments[index].type == type

        private fun MethodTrace.hasArgument(index: Int, type: String, value: String) =
            arguments[index].type == type && arguments[index].value == value
    }
}

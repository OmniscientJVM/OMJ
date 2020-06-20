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
import mu.KotlinLogging
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

        @Test
        fun `test int array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeIntArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar("com.agenttest.storeIntArray.Main", "[I", "i", null)
                },
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeIntArray.Main",
                        varType = "int",
                        arrayIndex = 0,
                        value = "6"
                    )
                }
            )
        }

        @Test
        fun `test int array store one-liner`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeIntArrayOneLiner.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeIntArrayOneLiner.Main",
                        varType = "int",
                        arrayIndex = 0,
                        value = "6"
                    )
                },
                {
                    it.storeVar("com.agenttest.storeIntArrayOneLiner.Main", "[I", "i", null)
                }
            )
        }

        @Test
        fun `test double array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeDoubleArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar("com.agenttest.storeDoubleArray.Main", "[D", "d", null)
                },
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeDoubleArray.Main",
                        varType = "double",
                        arrayIndex = 0,
                        value = "4.2"
                    )
                }
            )
        }

        @Test
        fun `test double array store one-liner`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeDoubleArrayOneLiner.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeDoubleArrayOneLiner.Main",
                        varType = "double",
                        arrayIndex = 0,
                        value = "4.2"
                    )
                },
                {
                    it.storeVar("com.agenttest.storeDoubleArrayOneLiner.Main", "[D", "d", null)
                }
            )
        }

        @Test
        fun `test object array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeObjectArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar(
                        "com.agenttest.storeObjectArray.Main",
                        "[Ljava.lang.Object;",
                        "o",
                        null
                    )
                },
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeObjectArray.Main",
                        varType = "java.lang.Object",
                        arrayIndex = 0,
                        value = null
                    )
                }
            )
        }

        @Test
        fun `test object array store one-liner`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeObjectArrayOneLiner.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeObjectArrayOneLiner.Main",
                        varType = "java.lang.Object",
                        arrayIndex = 0,
                        value = null
                    )
                },
                {
                    it.storeVar(
                        "com.agenttest.storeObjectArrayOneLiner.Main",
                        "[Ljava.lang.Object;",
                        "o",
                        null
                    )
                }
            )
        }

        @Test
        fun `test string array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeStringArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar(
                        "com.agenttest.storeStringArray.Main",
                        "[Ljava.lang.String;",
                        "o",
                        null
                    )
                },
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeStringArray.Main",
                        varType = "java.lang.String",
                        arrayIndex = 0,
                        value = "Hello"
                    )
                }
            )
        }

        @Test
        fun `test string array store one-liner`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeStringArrayOneLiner.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeStringArrayOneLiner.Main",
                        varType = "java.lang.String",
                        arrayIndex = 0,
                        value = "Hello"
                    )
                },
                {
                    it.storeVar(
                        "com.agenttest.storeStringArrayOneLiner.Main",
                        "[Ljava.lang.String;",
                        "o",
                        null
                    )
                }
            )
        }

        @Test
        fun `test 2D int array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeMultiIntArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar("com.agenttest.storeMultiIntArray.Main", "[[I", "i", null)
                },
                {
                    it.storeArray(
                        "com.agenttest.storeMultiIntArray.Main",
                        "int",
                        0,
                        "6"
                    )
                }
            )
        }

        @Test
        fun `test 3D int array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeMultiIntArray3.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar("com.agenttest.storeMultiIntArray3.Main", "[[[I", "i", null)
                },
                {
                    it.storeArray(
                        "com.agenttest.storeMultiIntArray3.Main",
                        "int",
                        0,
                        "6"
                    )
                }
            )
        }

        @Test
        fun `test boolean array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeBooleanArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar("com.agenttest.storeBooleanArray.Main", "[Z", "b", null)
                },
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeBooleanArray.Main",
                        varType = "boolean",
                        arrayIndex = 0,
                        value = "true"
                    )
                }
            )
        }

        @Test
        fun `test byte array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeByteArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar("com.agenttest.storeByteArray.Main", "[B", "b", null)
                },
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeByteArray.Main",
                        varType = "byte",
                        arrayIndex = 0,
                        value = "250"
                    )
                }
            )
        }

        @Test
        fun `test char array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeCharArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar("com.agenttest.storeCharArray.Main", "[C", "c", null)
                },
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeCharArray.Main",
                        varType = "char",
                        arrayIndex = 0,
                        value = "Q"
                    )
                }
            )
        }

        @Test
        fun `test float array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeFloatArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar("com.agenttest.storeFloatArray.Main", "[F", "f", null)
                },
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeFloatArray.Main",
                        varType = "float",
                        arrayIndex = 0,
                        value = "2.3"
                    )
                }
            )
        }

        @Test
        fun `test long array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeLongArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar("com.agenttest.storeLongArray.Main", "[J", "l", null)
                },
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeLongArray.Main",
                        varType = "long",
                        arrayIndex = 0,
                        value = "123456789123456789"
                    )
                }
            )
        }

        @Test
        fun `test short array store`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_storeShortArray.jar")

            traces.shouldHaveInOrder(
                {
                    it.storeVar("com.agenttest.storeShortArray.Main", "[S", "s", null)
                },
                {
                    it.storeArray(
                        containingClass = "com.agenttest.storeShortArray.Main",
                        varType = "short",
                        arrayIndex = 0,
                        value = "12345"
                    )
                }
            )
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

    @Nested
    inner class PutFieldTraceTests {

        @Test
        fun `test put boolean`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putBooleanField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putBooleanField.Main",
                    "boolean",
                    "com.agenttest.putBooleanField.Main.b",
                    "true"
                )
            }
        }

        @Test
        fun `test put byte`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putByteField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putByteField.Main",
                    "byte",
                    "com.agenttest.putByteField.Main.b",
                    "250"
                )
            }
        }

        @Test
        fun `test put char`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putCharField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putCharField.Main",
                    "char",
                    "com.agenttest.putCharField.Main.c",
                    "Q"
                )
            }
        }

        @Test
        fun `test put double`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putDoubleField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putDoubleField.Main",
                    "double",
                    "com.agenttest.putDoubleField.Main.d",
                    "4.2"
                )
            }
        }

        @Test
        fun `test put float`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putFloatField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putFloatField.Main",
                    "float",
                    "com.agenttest.putFloatField.Main.f",
                    "2.3"
                )
            }
        }

        @Test
        fun `test put int`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putIntField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putIntField.Main",
                    "int",
                    "com.agenttest.putIntField.Main.i",
                    "7"
                )
            }
        }

        @Test
        fun `test put long`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putLongField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putLongField.Main",
                    "long",
                    "com.agenttest.putLongField.Main.l",
                    "123456789123456789"
                )
            }
        }

        @Test
        fun `test put object`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putObjectField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putObjectField.Main",
                    "java.lang.Object",
                    "com.agenttest.putObjectField.Main.o",
                    null
                )
            }
        }

        @Test
        fun `test put short`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putShortField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putShortField.Main",
                    "short",
                    "com.agenttest.putShortField.Main.s",
                    "12345"
                )
            }
        }

        @Test
        fun `test put string`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putStringField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putStringField.Main",
                    "java.lang.String",
                    "com.agenttest.putStringField.Main.s",
                    "Hello"
                )
            }
        }
    }

    @Nested
    inner class PutStaticFieldTraceTests {

        @Test
        fun `test put boolean`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putBooleanStaticField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putBooleanStaticField.Main",
                    "boolean",
                    "com.agenttest.putBooleanStaticField.Main.b",
                    "true"
                )
            }
        }

        @Test
        fun `test put byte`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putByteStaticField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putByteStaticField.Main",
                    "byte",
                    "com.agenttest.putByteStaticField.Main.b",
                    "250"
                )
            }
        }

        @Test
        fun `test put char`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putCharStaticField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putCharStaticField.Main",
                    "char",
                    "com.agenttest.putCharStaticField.Main.c",
                    "Q"
                )
            }
        }

        @Test
        fun `test put double`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putDoubleStaticField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putDoubleStaticField.Main",
                    "double",
                    "com.agenttest.putDoubleStaticField.Main.d",
                    "4.2"
                )
            }
        }

        @Test
        fun `test put float`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putFloatStaticField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putFloatStaticField.Main",
                    "float",
                    "com.agenttest.putFloatStaticField.Main.f",
                    "2.3"
                )
            }
        }

        @Test
        fun `test put int`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putIntStaticField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putIntStaticField.Main",
                    "int",
                    "com.agenttest.putIntStaticField.Main.i",
                    "7"
                )
            }
        }

        @Test
        fun `test put long`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putLongStaticField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putLongStaticField.Main",
                    "long",
                    "com.agenttest.putLongStaticField.Main.l",
                    "123456789123456789"
                )
            }
        }

        @Test
        fun `test put object`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putObjectStaticField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putObjectStaticField.Main",
                    "java.lang.Object",
                    "com.agenttest.putObjectStaticField.Main.o",
                    null
                )
            }
        }

        @Test
        fun `test put short`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putShortStaticField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putShortStaticField.Main",
                    "short",
                    "com.agenttest.putShortStaticField.Main.s",
                    "12345"
                )
            }
        }

        @Test
        fun `test put string`(@TempDir tempDir: File) {
            val traces = generateTraces(tempDir, "agent-test_putStringStaticField.jar")

            traces.shouldExist {
                it.storeVar(
                    "com.agenttest.putStringStaticField.Main",
                    "java.lang.String",
                    "com.agenttest.putStringStaticField.Main.s",
                    "Hello"
                )
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

        private val logger = KotlinLogging.logger { }

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

            logger.debug {
                """
                |Files in temp dir:
                |${tempDir.walkTopDown().joinToString("\n")}
                """.trimMargin()
            }

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
        ) = this is StoreTrace &&
            callerClass == containingClass &&
            typeValuePair.type == varType &&
            variableName == name &&
            value?.let { typeValuePair.value == it } ?: true

        /**
         * Checks there is a store with a value of [value] into an array of type [varType] at index
         * [arrayIndex].
         *
         * @param containingClass The class the store happens in.
         * @param varType The type of the variable the value was stored in.
         * @param arrayIndex The index in the array the value was stored in.
         * @param value The value that was stored. Set to null if you don't care about the value.
         */
        private fun Trace.storeArray(
            containingClass: String,
            varType: String,
            arrayIndex: Int,
            value: String?
        ) = this is ArrayStoreTrace &&
            callerClass == containingClass &&
            this.arrayIndex == arrayIndex &&
            typeValuePair.type == varType &&
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

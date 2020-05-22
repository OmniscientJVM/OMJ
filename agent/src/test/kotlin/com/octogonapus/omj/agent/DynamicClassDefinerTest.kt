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

import java.io.File
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.objectweb.asm.Type

internal class DynamicClassDefinerTest {

    @Test
    fun generateBooleanContainer() {
        val body = """
            $imports
            final public class OMJ_Generated_Z extends MethodTrace {
            private int boolean_counter = 0;
            private boolean boolean_0;
            public OMJ_Generated_Z(final String methodLocation) {
            super(methodLocation);
            }
            @Override
            public void set_argument_boolean(final boolean value) {
            switch (boolean_counter) {
            case 0: boolean_0 = value;
            }
            boolean_counter++;
            }
            @Override
            public void serialize(final OutputStream outputStream) throws IOException {
            $appendIndex
            ${writeMethodIdentifier()}
            $methodLocation
            ${writeNumberOfArguments(1)}
            ${writeBoolean("boolean_0")}
            }
            }
            """.trimIndent()
        assertEquals(body, DynamicClassDefiner(null, null).generateClassCodeForMethod(listOf(
                Type.getType(Boolean::class.java))
        ).body)
    }

    @Test
    fun generateIntDoubleDoubleContainer() {
        val body = """
            $imports
            final public class OMJ_Generated_IDD extends MethodTrace {
            private int int_counter = 0;
            private int int_0;
            private int double_counter = 0;
            private double double_0;
            private double double_1;
            public OMJ_Generated_IDD(final String methodLocation) {
            super(methodLocation);
            }
            @Override
            public void set_argument_int(final int value) {
            switch (int_counter) {
            case 0: int_0 = value;
            }
            int_counter++;
            }
            @Override
            public void set_argument_double(final double value) {
            switch (double_counter) {
            case 0: double_0 = value;
            case 1: double_1 = value;
            }
            double_counter++;
            }
            @Override
            public void serialize(final OutputStream outputStream) throws IOException {
            $appendIndex
            ${writeMethodIdentifier()}
            $methodLocation
            ${writeNumberOfArguments(3)}
            ${writeInt("int_0")}
            ${writeDouble("double_0")}
            ${writeDouble("double_1")}
            }
            }
            """.trimIndent()
        assertEquals(body, DynamicClassDefiner(null, null).generateClassCodeForMethod(listOf(
                Type.getType(Int::class.java),
                Type.getType(Double::class.java),
                Type.getType(Double::class.java)
        )).body)
    }

    @Test
    fun generateObjectContainer() {
        val body = """
            $imports
            final public class OMJ_Generated_L extends MethodTrace {
            private int Object_counter = 0;
            private Object Object_0;
            public OMJ_Generated_L(final String methodLocation) {
            super(methodLocation);
            }
            @Override
            public void set_argument_Object(final Object value) {
            switch (Object_counter) {
            case 0: Object_0 = value;
            }
            Object_counter++;
            }
            @Override
            public void serialize(final OutputStream outputStream) throws IOException {
            $appendIndex
            ${writeMethodIdentifier()}
            $methodLocation
            ${writeNumberOfArguments(1)}
            ${writeObjectName("Object_0")}
            ${writeHashCode("Object_0")}
            }
            }
            """.trimIndent()
        assertEquals(
                body, DynamicClassDefiner(null, null).generateClassCodeForMethod(listOf(
                Type.getType(java.lang.Object::class.java))
        ).body)
    }

    @Test
    fun generateStringContainer() {
        val body = """
            $imports
            final public class OMJ_Generated_L extends MethodTrace {
            private int Object_counter = 0;
            private Object Object_0;
            public OMJ_Generated_L(final String methodLocation) {
            super(methodLocation);
            }
            @Override
            public void set_argument_Object(final Object value) {
            switch (Object_counter) {
            case 0: Object_0 = value;
            }
            Object_counter++;
            }
            @Override
            public void serialize(final OutputStream outputStream) throws IOException {
            $appendIndex
            ${writeMethodIdentifier()}
            $methodLocation
            ${writeNumberOfArguments(1)}
            ${writeObjectName("Object_0")}
            ${writeStringBytes("Object_0")}
            }
            }
            """.trimIndent()
        assertEquals(
                body, DynamicClassDefiner(null, null).generateClassCodeForMethod(listOf(
                Type.getType(java.lang.String::class.java))
        ).body)
    }

    @Test
    fun generateEmptyContainer() {
        val body = """
            $imports
            final public class OMJ_Generated_ extends MethodTrace {
            public OMJ_Generated_(final String methodLocation) {
            super(methodLocation);
            }
            @Override
            public void serialize(final OutputStream outputStream) throws IOException {
            $appendIndex
            ${writeMethodIdentifier()}
            $methodLocation
            ${writeNumberOfArguments(0)}
            }
            }
            """.trimIndent()
        assertEquals(body, DynamicClassDefiner(null, null).generateClassCodeForMethod(listOf()).body)
    }

    @Test
    fun generateAndCompileBooleanContainer(@TempDir tempDir: File) {
        val dynamicClassDefiner = DynamicClassDefiner(null, tempDir.toPath())
        val file = dynamicClassDefiner.writeToJarFile(
                dynamicClassDefiner.generateClassCodeForMethod(listOf(
                        Type.getType(Boolean::class.java))
                )
        )

        assertTrue(tempDir.toPath().resolve("OMJ_Generated_Z.java").toFile().exists())
        assertTrue(tempDir.toPath().resolve("OMJ_Generated_Z.class").toFile().exists())
        assertTrue(tempDir.toPath().resolve("OMJ_Generated_Z.jar").toFile().exists())
        assertEquals("OMJ_Generated_Z.jar", file.name)
    }

    @Suppress("SameParameterValue")
    companion object {
        const val imports = """import com.octogonapus.omj.agentlib.MethodTrace;
            import java.io.IOException;
            import java.io.OutputStream;"""

        const val appendIndex = """outputStream.write((byte) ((index >> 0) & 0xFF));
            outputStream.write((byte) ((index >> 8) & 0xFF));
            outputStream.write((byte) ((index >> 16) & 0xFF));
            outputStream.write((byte) ((index >> 24) & 0xFF));
            outputStream.write((byte) ((index >> 32) & 0xFF));
            outputStream.write((byte) ((index >> 40) & 0xFF));
            outputStream.write((byte) ((index >> 48) & 0xFF));
            outputStream.write((byte) ((index >> 56) & 0xFF));"""

        const val methodLocation = """outputStream.write(methodLocation.getBytes());
            outputStream.write(0);"""

        private fun writeObjectName(name: String): String =
                """outputStream.write('L');
            outputStream.write($name.getClass().getName().getBytes());
            outputStream.write(0);"""

        private fun writeHashCode(name: String): String =
                """final int ${name}_hashCode = System.identityHashCode($name);
            outputStream.write((byte) ((${name}_hashCode >> 0) & 0xFF));
            outputStream.write((byte) ((${name}_hashCode >> 8) & 0xFF));
            outputStream.write((byte) ((${name}_hashCode >> 16) & 0xFF));
            outputStream.write((byte) ((${name}_hashCode >> 24) & 0xFF));"""

        private fun writeStringBytes(name: String): String =
                """final byte[] ${name}_string_bytes = ((String) $name).getBytes();
            outputStream.write((byte) ((${name}_string_bytes.length >> 0) & 0xFF));
            outputStream.write((byte) ((${name}_string_bytes.length >> 8) & 0xFF));
            outputStream.write((byte) ((${name}_string_bytes.length >> 16) & 0xFF));
            outputStream.write((byte) ((${name}_string_bytes.length >> 24) & 0xFF));
            outputStream.write(${name}_string_bytes);"""

        private fun writeNumberOfArguments(numberOfArguments: Int): String =
                """outputStream.write($numberOfArguments);"""

        private fun writeInt(name: String): String =
                """outputStream.write('I');
            outputStream.write((byte) (($name >> 0) & 0xFF));
            outputStream.write((byte) (($name >> 8) & 0xFF));
            outputStream.write((byte) (($name >> 16) & 0xFF));
            outputStream.write((byte) (($name >> 24) & 0xFF));"""

        private fun writeDouble(name: String): String =
                """outputStream.write('D');
            final long ${name}_l = Double.doubleToRawLongBits($name);
            outputStream.write((byte) ((${name}_l >> 0) & 0xFF));
            outputStream.write((byte) ((${name}_l >> 8) & 0xFF));
            outputStream.write((byte) ((${name}_l >> 16) & 0xFF));
            outputStream.write((byte) ((${name}_l >> 24) & 0xFF));
            outputStream.write((byte) ((${name}_l >> 32) & 0xFF));
            outputStream.write((byte) ((${name}_l >> 40) & 0xFF));
            outputStream.write((byte) ((${name}_l >> 48) & 0xFF));
            outputStream.write((byte) ((${name}_l >> 56) & 0xFF));"""

        private fun writeMethodIdentifier(): String = """outputStream.write(0x2);"""

        private fun writeBoolean(name: String): String =
                """outputStream.write('Z');
            outputStream.write($name ? 1 : 0);"""
    }
}

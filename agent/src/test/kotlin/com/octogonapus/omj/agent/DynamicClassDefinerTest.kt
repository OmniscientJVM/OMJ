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
            $methodLocationBytes
            final int remainingLength = 1 + methodLocationBytes.length + 2;
            $appendRemainingLength
            outputStream.write(0x2);
            outputStream.write(methodLocationBytes);
            outputStream.write('Z');
            outputStream.write(boolean_0 ? 1 : 0);
            }
            }
            """.trimIndent()
        assertEquals(body, DynamicClassDefiner.generateClassCodeForMethod("(Z)V").body)
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
            $methodLocationBytes
            final int remainingLength = 1 + methodLocationBytes.length + 23;
            $appendRemainingLength
            outputStream.write(0x2);
            outputStream.write(methodLocationBytes);
            outputStream.write('I');
            outputStream.write((byte) (int_0 >> 0));
            outputStream.write((byte) (int_0 >> 8));
            outputStream.write((byte) (int_0 >> 16));
            outputStream.write((byte) (int_0 >> 24));
            outputStream.write('D');
            outputStream.write((byte) (double_0 >> 0));
            outputStream.write((byte) (double_0 >> 8));
            outputStream.write((byte) (double_0 >> 16));
            outputStream.write((byte) (double_0 >> 24));
            outputStream.write((byte) (double_0 >> 32));
            outputStream.write((byte) (double_0 >> 40));
            outputStream.write((byte) (double_0 >> 48));
            outputStream.write((byte) (double_0 >> 56));
            outputStream.write('D');
            outputStream.write((byte) (double_1 >> 0));
            outputStream.write((byte) (double_1 >> 8));
            outputStream.write((byte) (double_1 >> 16));
            outputStream.write((byte) (double_1 >> 24));
            outputStream.write((byte) (double_1 >> 32));
            outputStream.write((byte) (double_1 >> 40));
            outputStream.write((byte) (double_1 >> 48));
            outputStream.write((byte) (double_1 >> 56));
            }
            }
            """.trimIndent()
        assertEquals(body, DynamicClassDefiner.generateClassCodeForMethod("(IDD)V").body)
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
            $methodLocationBytes
            final int remainingLength = 1 + methodLocationBytes.length + 22;
            $appendRemainingLength
            outputStream.write(0x2);
            outputStream.write(methodLocationBytes);
            outputStream.write('L');
            outputStream.write(Object_0.getClass().getName().getBytes());
            outputStream.write(0);
            final int Object_0_hashCode = System.identityHashCode(Object_0);
            outputStream.write((byte) (Object_0_hashCode >> 0));
            outputStream.write((byte) (Object_0_hashCode >> 8));
            outputStream.write((byte) (Object_0_hashCode >> 16));
            outputStream.write((byte) (Object_0_hashCode >> 24));
            }
            }
            """.trimIndent()
        assertEquals(
                body, DynamicClassDefiner.generateClassCodeForMethod("(Ljava/lang/String;)V").body)
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
            $methodLocationBytes
            final int remainingLength = 1 + methodLocationBytes.length + 0;
            $appendRemainingLength
            outputStream.write(0x2);
            outputStream.write(methodLocationBytes);
            }
            }
            """.trimIndent()
        assertEquals(body, DynamicClassDefiner.generateClassCodeForMethod("()V)").body)
    }

    @Test
    fun generateAndCompileBooleanContainer(@TempDir tempDir: File) {
        val file = DynamicClassDefiner(null, tempDir.toPath())
                .writeToJarFile(DynamicClassDefiner.generateClassCodeForMethod("(B)V"))
        assertTrue(tempDir.toPath().resolve("OMJ_Generated_B.java").toFile().exists())
        assertTrue(tempDir.toPath().resolve("OMJ_Generated_B.class").toFile().exists())
        assertTrue(tempDir.toPath().resolve("OMJ_Generated_B.jar").toFile().exists())
        assertEquals("OMJ_Generated_B.jar", file.name)
    }

    companion object {
        const val imports = """import com.octogonapus.omj.agentlib.MethodTrace;
            import java.io.IOException;
            import java.io.OutputStream;"""

        const val appendIndex = """outputStream.write((byte) (index >> 0));
            outputStream.write((byte) (index >> 8));
            outputStream.write((byte) (index >> 16));
            outputStream.write((byte) (index >> 24));
            outputStream.write((byte) (index >> 32));
            outputStream.write((byte) (index >> 40));
            outputStream.write((byte) (index >> 48));
            outputStream.write((byte) (index >> 56));"""

        const val appendRemainingLength = """outputStream.write((byte) (remainingLength >> 0));
            outputStream.write((byte) (remainingLength >> 8));
            outputStream.write((byte) (remainingLength >> 16));
            outputStream.write((byte) (remainingLength >> 24));"""

        const val methodLocationBytes =
                """final byte[] methodLocationBytes = methodLocation.getBytes();"""
    }
}

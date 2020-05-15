package com.octogonapus.omj.agent

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException

internal class DynamicClassDefinerTest {

    @Test
    fun generateBooleanContainer() {
        assertEquals(booleanBody, DynamicClassDefiner.generateClassCodeForMethod("(Z)V").body)
    }

    @Test
    fun generateIntDoubleDoubleContainer() {
        val body = """
            import com.octogonapus.omj.agentlib.MethodTrace;
            final public class OMJ_Generated_IDD implements MethodTrace {
            private int double_counter = 0;
            private double double_0;
            private double double_1;
            private int int_counter = 0;
            private int int_0;
            @Override
            public void set_argument_double(final double value) {
            switch (double_counter) {
            case 0: double_0 = value;
            case 1: double_1 = value;
            }
            double_counter++;
            }
            @Override
            public void set_argument_int(final int value) {
            switch (int_counter) {
            case 0: int_0 = value;
            }
            int_counter++;
            }
            }
            """.trimIndent()
        assertEquals(body, DynamicClassDefiner.generateClassCodeForMethod("(IDD)V").body)
    }

    @Test
    fun generateStringContainer() {
        val body = """
            import com.octogonapus.omj.agentlib.MethodTrace;
            final public class OMJ_Generated_L implements MethodTrace {
            private int Object_counter = 0;
            private Object Object_0;
            @Override
            public void set_argument_Object(final Object value) {
            switch (Object_counter) {
            case 0: Object_0 = value;
            }
            Object_counter++;
            }
            }
            """.trimIndent()
        assertEquals(
                body, DynamicClassDefiner.generateClassCodeForMethod("(Ljava/lang/String;)V").body)
    }

    @Test
    fun generateEmptyContainer() {
        val body = """
            import com.octogonapus.omj.agentlib.MethodTrace;
            final public class OMJ_Generated_ implements MethodTrace {
            }
            """.trimIndent()
        assertEquals(body, DynamicClassDefiner.generateClassCodeForMethod("()V)").body)
    }

    @Test
    @Throws(IOException::class)
    fun generateAndCompileBooleanContainer(@TempDir tempDir: File) {
        val file = DynamicClassDefiner(null, tempDir.toPath())
                .writeToJarFile(DynamicClassDefiner.generateClassCodeForMethod("(B)V"))
        assertTrue(tempDir.toPath().resolve("OMJ_Generated_B.java").toFile().exists())
        assertTrue(tempDir.toPath().resolve("OMJ_Generated_B.class").toFile().exists())
        assertTrue(tempDir.toPath().resolve("OMJ_Generated_B.jar").toFile().exists())
        assertEquals("OMJ_Generated_B.jar", file.name)
    }

    companion object {
        private val booleanBody = """
            import com.octogonapus.omj.agentlib.MethodTrace;
            final public class OMJ_Generated_Z implements MethodTrace {
            private int boolean_counter = 0;
            private boolean boolean_0;
            @Override
            public void set_argument_boolean(final boolean value) {
            switch (boolean_counter) {
            case 0: boolean_0 = value;
            }
            boolean_counter++;
            }
            }
            """.trimIndent()
    }
}

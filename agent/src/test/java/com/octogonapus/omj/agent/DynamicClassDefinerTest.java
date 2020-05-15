package com.octogonapus.omj.agent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicClassDefinerTest {

    private static final String booleanBody = "import com.octogonapus.omj.agentlib.MethodTrace;\n" +
                                              "final public class OMJ_Generated_Z implements " +
                                              "MethodTrace" + " {\n" +
                                              "private int boolean_counter = 0;\n" +
                                              "private boolean boolean_0;\n" + "@Override\n" +
                                              "public void set_argument_boolean(final boolean " +
                                              "value) {\n" + "switch (boolean_counter) {\n" +
                                              "case 0: boolean_0 = value;\n" + "}\n" +
                                              "boolean_counter++;\n" + "}\n" + "}";


    @Test
    void generateBooleanContainer() {
        assertEquals(booleanBody, DynamicClassDefiner.generateClassCodeForMethod("(Z)V").body);
    }

    @Test
    void generateIntDoubleDoubleContainer() {
        final String body = "import com.octogonapus.omj.agentlib.MethodTrace;\n" +
                            "final public class OMJ_Generated_IDD implements MethodTrace {\n" +
                            "private int double_counter = 0;\n" + "private double double_0;\n" +
                            "private double double_1;\n" + "private int int_counter = 0;\n" +
                            "private int int_0;\n" + "@Override\n" +
                            "public void set_argument_double(final double value) {\n" +
                            "switch (double_counter) {\n" + "case 0: double_0 = value;\n" +
                            "case 1: double_1 = value;\n" + "}\n" + "double_counter++;\n" + "}\n" +
                            "@Override\n" + "public void set_argument_int(final int value) {\n" +
                            "switch (int_counter) {\n" + "case 0: int_0 = value;\n" + "}\n" +
                            "int_counter++;\n" + "}\n" + "}";
        assertEquals(body, DynamicClassDefiner.generateClassCodeForMethod("(IDD)V").body);
    }

    @Test
    void generateStringContainer() {
        final String body = "import com.octogonapus.omj.agentlib.MethodTrace;\n" +
                            "final public class OMJ_Generated_L implements MethodTrace {\n" +
                            "private int Object_counter = 0;\n" + "private Object Object_0;\n" +
                            "@Override\n" +
                            "public void set_argument_Object(final Object value) {\n" +
                            "switch (Object_counter) {\n" + "case 0: Object_0 = value;\n" + "}\n" +
                            "Object_counter++;\n" + "}\n" + "}";
        assertEquals(body,
                     DynamicClassDefiner.generateClassCodeForMethod("(Ljava/lang/String;)V").body);
    }

    @Test
    void generateEmptyContainer() {
        final String body = "import com.octogonapus.omj.agentlib.MethodTrace;\n" +
                            "final public class OMJ_Generated_ implements MethodTrace {\n" + "}";
        assertEquals(body, DynamicClassDefiner.generateClassCodeForMethod("()V)").body);
    }

    @Test
    void generateAndCompileBooleanContainer(@TempDir final File tempDir) throws IOException {
        final var file = new DynamicClassDefiner(null, tempDir.toPath()).writeAllToJarFile(
                DynamicClassDefiner.generateClassCodeForMethod("(B)V"));
        assertTrue(tempDir.toPath().resolve("OMJ_Generated_B.java").toFile().exists());
        assertTrue(tempDir.toPath().resolve("OMJ_Generated_B.class").toFile().exists());
        assertTrue(tempDir.toPath().resolve("OMJ_Generated_B.jar").toFile().exists());
        assertEquals("OMJ_Generated_B.jar", file.getName());
    }
}

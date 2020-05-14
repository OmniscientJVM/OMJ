package com.octogonapus.omj.agent;

import com.octogonapus.omj.agent.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarFile;

public class DynamicClassDefiner {

    private final Set<String> dynamicClasses = new HashSet<>();
    private final Instrumentation instrumentation;

    public DynamicClassDefiner(final Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    void defineClassForMethod(final String methodDescriptor) {
        if (dynamicClasses.add(methodDescriptor)) {
            final var dynamicClass = generateClassCodeForMethod(methodDescriptor);
            try {
                instrumentation.appendToSystemClassLoaderSearch(new JarFile(writeAllToJarFile(
                        dynamicClass)));
            } catch (IOException e) {
                // This is a total system failure because the instrumented code needs this class
                // to run.
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private File writeAllToJarFile(final DynamicClass dynamicClass) throws IOException {
        final var jarFile = Util.cacheDir.resolve(dynamicClass.name + ".jar").toFile();
        jarFile.deleteOnExit();
        if (!jarFile.exists() && jarFile.createNewFile()) {
            throw new IOException(
                    "Failed to create the new dynamic_classes jar file when it did not previously" +
                    " exist.");
        }

        throw new UnsupportedOperationException("Not implemented.");
    }

    private DynamicClass generateClassCodeForMethod(final String methodDescriptor) {
        final var argumentTypes = Parser.parseMethodDescriptor(methodDescriptor).argumentTypes;
        final StringBuilder stringBuilder = new StringBuilder();
        for (char c : argumentTypes) {
            stringBuilder.append(c);
        }
        final var argumentString = stringBuilder.toString();

        throw new UnsupportedOperationException("Not implemented.");
    }

    private static class DynamicClass {
        String name;
        String body;
    }
}

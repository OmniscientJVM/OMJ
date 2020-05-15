package com.octogonapus.omj.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.objectweb.asm.Type;

final class DynamicClassDefiner {

  private final Set<String> namesOfDynamicClasses = new HashSet<>();
  private final Instrumentation instrumentation;
  private final Path cacheDir;
  private static final Path javaBin =
      Paths.get("/home/salmon/zulu14.28.21-ca-jdk14.0.1-linux_x64/bin");

  DynamicClassDefiner(final Instrumentation instrumentation, final Path cacheDir) {
    this.instrumentation = instrumentation;
    this.cacheDir = cacheDir;
  }

  /**
   * Ensures that a container class exists for the method. This method is idempotent.
   *
   * @param methodDescriptor The method descriptor to generate a class for.
   * @return The name of the generated class. It will be in the unnamed package.
   */
  String defineClassForMethod(final String methodDescriptor) {
    final String className = generateClassName(Type.getArgumentTypes(methodDescriptor));

    // Check if the class has already been generated by checking if we saved its name
    if (namesOfDynamicClasses.add(className)) {
      final var dynamicClass = generateClassCodeForMethod(methodDescriptor);

      try {
        instrumentation.appendToSystemClassLoaderSearch(new JarFile(writeToJarFile(dynamicClass)));
      } catch (IOException e) {
        // This is a total system failure because the instrumented code needs this class
        // to run.
        e.printStackTrace();
        System.exit(1);
      }

      return dynamicClass.name;
    }

    return className;
  }

  /**
   * Compiles the generated class and puts it into a Jar file. Meant for internal use; you should
   * call {@link #defineClassForMethod(String)} instead.
   *
   * @param dynamicClass The generated class to compile and put into a Jar file.
   * @return The Jar file.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  File writeToJarFile(final DynamicClass dynamicClass) throws IOException {
    final var jarFile = cacheDir.resolve(dynamicClass.name + ".jar").toFile();
    jarFile.deleteOnExit();
    jarFile.delete();

    final var sourceFile = cacheDir.resolve(dynamicClass.name + ".java").toFile();
    sourceFile.deleteOnExit();
    sourceFile.delete();
    sourceFile.createNewFile();

    try (final var os = new FileOutputStream(sourceFile)) {
      os.write(dynamicClass.body.getBytes());
    }

    final Process javac =
        new ProcessBuilder(
                javaBin.resolve("javac").toString(),
                "-classpath",
                AgentLibJarExtractor.extractJar().getAbsolutePath(),
                sourceFile.getAbsolutePath())
            .inheritIO()
            .start();
    try {
      javac.waitFor();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    cacheDir.resolve(dynamicClass.name + ".class").toFile().deleteOnExit();

    final Process jar =
        new ProcessBuilder(
                javaBin.resolve("jar").toString(),
                "cf",
                jarFile.getName(),
                dynamicClass.name + ".class")
            .directory(cacheDir.toFile())
            .inheritIO()
            .start();
    try {
      jar.waitFor();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    return jarFile;
  }

  /**
   * Generates the code to define a container class for a method.
   *
   * @param methodDescriptor The method descriptor.
   * @return The {@link DynamicClass} matching the method.
   */
  static DynamicClass generateClassCodeForMethod(final String methodDescriptor) {
    // TODO: Also generate serialization code
    final var argumentTypes = Type.getArgumentTypes(methodDescriptor);
    final StringBuilder classCodeBuilder = new StringBuilder();
    final String className = generateClassName(argumentTypes);

    final Map<String, Integer> typeClassNameCounts = new HashMap<>();
    for (final Type type : argumentTypes) {
      final String typeClassName = TypeUtil.getClassName(type);
      typeClassNameCounts.merge(typeClassName, 1, Integer::sum);
    }

    final var snippets =
        typeClassNameCounts.entrySet().stream()
            .map(
                entry -> {
                  final String typeClassName = entry.getKey();
                  final Integer count = entry.getValue();
                  return generateSnippet(typeClassName, count);
                })
            .collect(Collectors.toList());

    classCodeBuilder.append("import com.octogonapus.omj.agentlib.MethodTrace;\n");
    classCodeBuilder
        .append("final public class ")
        .append(className)
        .append(" implements MethodTrace {\n");
    snippets.forEach(it -> classCodeBuilder.append(it.fields));
    snippets.forEach(it -> classCodeBuilder.append(it.methods));
    classCodeBuilder.append("}");

    return new DynamicClass(className, classCodeBuilder.toString());
  }

  private static GeneratedSnippet generateSnippet(final String typeClassName, final Integer count) {
    final GeneratedSnippet snippet = new GeneratedSnippet();

    snippet.fields.append("private int ").append(typeClassName).append("_counter = 0;\n");
    for (int i = 0; i < count; i++) {
      snippet
          .fields
          .append("private ")
          .append(typeClassName)
          .append(' ')
          .append(typeClassName)
          .append('_')
          .append(i)
          .append(";\n");
    }

    snippet
        .methods
        .append("@Override\n")
        .append("public void set_argument_")
        .append(typeClassName)
        .append("(final ")
        .append(typeClassName)
        .append(" value) {\n")
        .append("switch (")
        .append(typeClassName)
        .append("_counter) {\n");
    for (int i = 0; i < count; i++) {
      snippet
          .methods
          .append("case ")
          .append(i)
          .append(": ")
          .append(typeClassName)
          .append('_')
          .append(i)
          .append(" = value;\n");
    }
    snippet.methods.append("}\n").append(typeClassName).append("_counter++;\n").append("}\n");

    return snippet;
  }

  private static String generateClassName(final Type[] argumentTypes) {
    final StringBuilder classNameBuilder = new StringBuilder();

    classNameBuilder.append("OMJ_Generated_");

    for (Type c : argumentTypes) {
      classNameBuilder.append(TypeUtil.getShortenedDescriptor(c));
    }

    return classNameBuilder.toString();
  }

  static class DynamicClass {
    String name;
    String body;

    private DynamicClass(final String name, final String body) {
      this.name = name;
      this.body = body;
    }
  }

  private static class GeneratedSnippet {
    final StringBuilder fields = new StringBuilder();
    final StringBuilder methods = new StringBuilder();
  }
}

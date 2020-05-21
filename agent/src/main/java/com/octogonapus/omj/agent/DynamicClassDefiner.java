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
package com.octogonapus.omj.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.octogonapus.omj.util.SimpleTypeUtil;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DynamicClassDefiner {

  private final Logger logger = LoggerFactory.getLogger(DynamicClassDefiner.class);
  private static final Path javaBin = Paths.get(System.getProperty("java.home")).resolve("bin");
  private final Set<String> namesOfDynamicClasses = new HashSet<>();
  private final Instrumentation instrumentation;
  private final Path cacheDir;

  DynamicClassDefiner(final Instrumentation instrumentation, final Path cacheDir) {
    this.instrumentation = instrumentation;
    this.cacheDir = cacheDir;
  }

  private List<Type> getMethodSignatureTypes(final String methodDescriptor, final boolean isStatic) {
    final var out = new ArrayList<Type>();
    if (!isStatic) {
      out.add(Type.getType(Object.class));
    }
    out.addAll(Arrays.asList(Type.getArgumentTypes(methodDescriptor)));
    return out;
  }

  /**
   * Ensures that a container class exists for the method. This method is idempotent.
   *
   * @param methodDescriptor The method descriptor to generate a class for.
   * @param isStatic Whether the method is static.
   * @return The name of the generated class. It will be in the unnamed package.
   */
  String defineClassForMethod(final String methodDescriptor, final boolean isStatic) {
    final List<Type> methodSignatureTypes = getMethodSignatureTypes(methodDescriptor, isStatic);
    final String className = generateClassName(methodSignatureTypes);

    // Check if the class has already been generated by checking if we saved its name
    if (namesOfDynamicClasses.add(className)) {
      final var dynamicClass = generateClassCodeForMethod(methodSignatureTypes);
      logger.debug("Generated dynamic class {}", dynamicClass.name);

      try {
        final File jarFile = writeToJarFile(dynamicClass);
        logger.debug("Wrote to jar file: {}", jarFile.getAbsolutePath());
        instrumentation.appendToSystemClassLoaderSearch(new JarFile(jarFile));
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
   * call {@link #defineClassForMethod(String, boolean)} instead.
   *
   * @param dynamicClass The generated class to compile and put into a Jar file.
   * @return The Jar file.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  File writeToJarFile(final DynamicClass dynamicClass) throws IOException {
    final var jarFile = cacheDir.resolve(dynamicClass.name + ".jar").toFile();
    jarFile.deleteOnExit();
    jarFile.delete();

    logger.debug("Writing dynamic class {} to Jar file {}:\n{}", dynamicClass.name, jarFile.getAbsolutePath(), dynamicClass.toString());

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
   * @param argumentTypes The types of the arguments this class needs to keep track of, including
   *                      the receiver if applicable.
   * @return The {@link DynamicClass} matching the method.
   */
  static DynamicClass generateClassCodeForMethod(final List<Type> argumentTypes) {
    final StringBuilder classCodeBuilder = new StringBuilder();
    final String className = generateClassName(argumentTypes);

    // Sum the number of times each argument type appears in the method
    final Map<Integer, Integer> typeClassNameCounts = new LinkedHashMap<>();
    for (final Type type : argumentTypes) {
      typeClassNameCounts.merge(type.getSort(), 1, Integer::sum);
    }

    // Generate a snippet for each argument type
    final var snippets =
        typeClassNameCounts.entrySet().stream()
            .map(entry -> {
              final Integer sort = entry.getKey();

              final Type argumentTypeFromSort = argumentTypes.stream()
                  .filter(type -> type.getSort() == sort)
                  .findFirst()
                  .orElseThrow();

              return generateSetArgumentOverride(TypeUtil.getSimpleType(argumentTypeFromSort),
                                                 entry.getValue());
            })
            .collect(Collectors.toList());

    // Import the class we will extend
    classCodeBuilder.append("import com.octogonapus.omj.agentlib.MethodTrace;\n");

    // Needed for the serialize method override
    classCodeBuilder.append("import java.io.IOException;\nimport java.io.OutputStream;\n");

    // Declare the class
    classCodeBuilder
        .append("final public class ")
        .append(className)
        .append(" extends MethodTrace {\n");

    // Append all the fields
    snippets.forEach(
        it ->
            classCodeBuilder.append(
                Stream.concat(it.utilityFields.stream(), it.argumentFields.stream())
                    .map(field -> field.contents)
                    .collect(Collectors.joining())));

    // Declare a constructor that takes the method location data. The location is generated by the
    // instrumentation.
    classCodeBuilder
        .append("public ")
        .append(className)
        .append("(final String methodLocation) {\n")
        .append("super(methodLocation);\n")
        .append("}\n");

    // Append all the methods
    snippets.forEach(it -> classCodeBuilder.append(it.methods));

    // Append the serialize method override
    classCodeBuilder.append(generateSerializeOverride(snippets));

    // Close the class declaration
    classCodeBuilder.append('}');

    return new DynamicClass(className, classCodeBuilder.toString());
  }

  /**
   * Generates a single {@link GeneratedSnippet} given the type of an argument and the number of
   * times that type is used between all the method's arguments. Only one method is generated that
   * overrides the appropriate {@link com.octogonapus.omj.agentlib.MethodTrace} method, but that
   * method contains a switch that handles every expected invocation of the method. The number of
   * expected invocations is the {@code count}.
   *
   * @param argumentType The type of an argument in the method being traced.
   * @param count The number of arguments with the provided type.
   * @return A {@link GeneratedSnippet} that handles all arguments of the provided type.
   */
  private static GeneratedSnippet generateSetArgumentOverride(
      final SimpleTypeUtil.SimpleType argumentType, final Integer count) {
    final GeneratedSnippet snippet = new GeneratedSnippet();
    final String typeClassName = SimpleTypeUtil.getAdaptedClassName(argumentType);

    // Counter that keeps track of the number of times the set_argument_xxx method has been called
    final var counterField = new Field();
    counterField.name = typeClassName + "_counter";
    counterField.contents = "private int " + counterField.name + " = 0;\n";
    snippet.utilityFields.add(counterField);

    // Generate a field for each expected invocation
    for (int i = 0; i < count; i++) {
      final var field = new Field();
      field.type = argumentType;
      field.name = typeClassName + '_' + i;
      field.contents = "private " + typeClassName + ' ' + field.name + ";\n";
      snippet.argumentFields.add(field);
    }

    // Override the appropriate method and generate a switch that handles every expected invocation
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
      // Set the appropriate field based on the invocation number
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

    // Increment the counter after the switch
    snippet.methods.append("}\n").append(typeClassName).append("_counter++;\n").append("}\n");

    return snippet;
  }

  private static StringBuilder generateSerializeOverride(final List<GeneratedSnippet> snippets) {
    final var builder = new StringBuilder();

    // Imports for OutputStream and IOException are added at a higher level
    builder
        .append("@Override\n")
        .append("public void serialize(final OutputStream outputStream) throws IOException {\n");

    // The index is given in the serialize method's parameters
    appendValueAsBytes("index",
                       SimpleTypeUtil.getLengthOfTypeForTrace(SimpleTypeUtil.SimpleType.LONG),
                       builder);

    // Method trace identifier
    builder.append("outputStream.write(0x2);\n");

    // Location
    builder.append("outputStream.write(methodLocation.getBytes());\n");
    builder.append("outputStream.write(0);\n");

    // Number of arguments
    final int numberOfArguments = snippets.stream().mapToInt(snippet -> snippet.argumentFields.size()).sum();
    if (numberOfArguments > 0xFF) {
      throw new IllegalStateException("Too many arguments to pack into one byte: " + numberOfArguments);
    }
    builder.append("outputStream.write(").append(numberOfArguments).append(");\n");

    // Type and value for each method argument
    for (final var snippet : snippets) {
      for (final var field : snippet.argumentFields) {
        appendField(field, builder);
      }
    }

    // End the serialize method
    builder.append("}\n");

    return builder;
  }

  /**
   * Appends the type and value of the field.
   *
   * @param field The field to process.
   * @param builder The builder to append to.
   */
  private static void appendField(final Field field, final StringBuilder builder) {
    builder.append("outputStream.write('")
        .append(SimpleTypeUtil.getDescriptorChar(field.type))
        .append("');\n");
    appendFieldValue(field, builder);
  }

  /**
   * Appends code to write the value of this field into the serialize method's output stream.
   *
   * @param field The field to write the value of.
   * @param builder The builder to append to.
   */
  private static void appendFieldValue(final Field field, final StringBuilder builder) {
    switch (field.type) {
      case VOID -> throw new IllegalStateException("Somehow got a field with type void.");
      case BOOLEAN ->
          builder.append("outputStream.write(").append(field.name).append(" ? 1 : 0);\n");
      case CHAR, LONG, INT, SHORT, BYTE ->
          appendValueAsBytes(field.name, SimpleTypeUtil.getLengthOfTypeForTrace(field.type), builder);
      case DOUBLE -> {
        builder.append("final long ").append(field.name).append("_l = Double.doubleToRawLongBits(").append(field.name).append(");\n");
        appendValueAsBytes(field.name + "_l", 8, builder);
      }
      case FLOAT -> {
        builder.append("final int ").append(field.name).append("_l = Float.floatToRawIntBits(").append(field.name).append(");\n");
        appendValueAsBytes(field.name + "_l", 4, builder);
      }
      case REFERENCE -> {
        builder.append("outputStream.write(")
            .append(field.name)
            .append(".getClass().getName().getBytes());\n");
        builder.append("outputStream.write(0);\n");
        builder
            .append("final int ")
            .append(field.name)
            .append("_hashCode = System.identityHashCode(")
            .append(field.name)
            .append(");\n");
        appendValueAsBytes(field.name + "_hashCode",
                           SimpleTypeUtil.getLengthOfTypeForTrace(SimpleTypeUtil.SimpleType.INT),
                           builder);
      }
    }
  }

  /**
   * Appends code to write the bytes of the variable to the serialize method's output stream. The
   * bytes are written to the output stream in little-endian format.
   *
   * @param varName The name of the variable whose bytes to write.
   * @param bytes The number of bytes to write.
   * @param builder The builder to append to.
   */
  private static void appendValueAsBytes(final String varName, final int bytes, final StringBuilder builder) {
    for (int i = 0; i < bytes; i++) {
      builder
          .append("outputStream.write((byte) ((")
          .append(varName)
          .append(" >> ")
          .append(i * 8)
          .append(") & 0xFF));\n");
    }
  }

  private static String generateClassName(final List<Type> argumentTypes) {
    final StringBuilder classNameBuilder = new StringBuilder();

    classNameBuilder.append("OMJ_Generated_");

    for (final Type c : argumentTypes) {
      classNameBuilder.append(TypeUtil.getDescriptorChar(c));
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

    @Override
    public String toString() {
      return "DynamicClass{" + "name='" + name + '\'' + ", body='" + body + '\'' + '}';
    }
  }

  private static class GeneratedSnippet {
    final List<Field> utilityFields = new ArrayList<>();
    final List<Field> argumentFields = new ArrayList<>();
    final StringBuilder methods = new StringBuilder();
  }

  private static class Field {
    SimpleTypeUtil.SimpleType type;
    String name;
    String contents;
  }
}

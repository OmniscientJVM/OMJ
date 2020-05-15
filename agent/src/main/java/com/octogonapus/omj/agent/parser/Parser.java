package com.octogonapus.omj.agent.parser;

import java.util.ArrayList;

class Parser {

  /**
   * Parses a method descriptor to extract its parameter list and return type.
   *
   * @param descriptor The descriptor string to parse, like "()V" or "(ID[Ljava/lang/Object;)B". See
   *     Section 4.3.3 for the definition.
   * @return The {@link ParsedMethodDescriptor}.
   */
  static ParsedMethodDescriptor parseMethodDescriptor(final String descriptor) {
    if (descriptor.isEmpty()) {
      throw new IllegalStateException("Descriptor is empty.");
    }

    final var parameters = new ArrayList<Character>();

    final var chars = descriptor.toCharArray();
    int i = 0;

    // Validate first character
    if (chars[i] != '(') {
      throw new IllegalStateException("Incorrect start of descriptor: " + chars[i]);
    }

    i++;

    // Parse parameters
    while (chars[i] != ')') {
      final var nextFieldType = scanNextFieldType(chars, i);
      parameters.add(nextFieldType.fieldType);
      i = nextFieldType.nextStartingIndex;

      if (i >= chars.length) {
        throw new IllegalStateException("Unexpected end of descriptor.");
      }
    }

    i++;

    // Parse return type
    final var returnTypePair = scanNextFieldType(chars, i);
    final var returnType = returnTypePair.fieldType;
    if (returnTypePair.nextStartingIndex != chars.length) {
      throw new IllegalStateException(
          "Descriptor continues past return type: "
              + descriptor
              + " (index "
              + returnTypePair.nextStartingIndex
              + ")");
    }

    return new ParsedMethodDescriptor(parameters, returnType);
  }

  private static NextFieldType scanNextFieldType(char[] descriptor, int startingIndex) {
    switch (descriptor[startingIndex]) {
      case 'B':
        // byte
        return new NextFieldType('B', startingIndex + 1);

      case 'C':
        // char
        return new NextFieldType('C', startingIndex + 1);

      case 'D':
        // double
        return new NextFieldType('D', startingIndex + 1);

      case 'F':
        // float
        return new NextFieldType('F', startingIndex + 1);

      case 'I':
        // int
        return new NextFieldType('I', startingIndex + 1);

      case 'J':
        // long
        return new NextFieldType('J', startingIndex + 1);

      case 'L':
        {
          // reference
          int i = startingIndex + 1;
          // Go to the end of the reference
          while (descriptor[i] != ';') {
            i++;
          }
          return new NextFieldType('L', i + 1);
        }

      case 'S':
        // short
        return new NextFieldType('S', startingIndex + 1);

      case 'Z':
        // boolean
        return new NextFieldType('Z', startingIndex + 1);

      case '[':
        {
          // array dimension
          int i = startingIndex + 1;
          // Go over the remaining array dimensions
          while (descriptor[i] == '[') {
            i++;
          }

          return new NextFieldType('L', scanNextFieldType(descriptor, i).nextStartingIndex);
        }

      case 'V':
        // void
        return new NextFieldType('V', startingIndex + 1);

      default:
        throw new IllegalStateException(
            "Unknown field type beginning: " + descriptor[startingIndex]);
    }
  }
}

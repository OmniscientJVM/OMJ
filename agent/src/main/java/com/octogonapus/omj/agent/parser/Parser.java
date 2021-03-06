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
package com.octogonapus.omj.agent.parser;

import java.util.ArrayList;

public final class Parser {

  /**
   * Parses a field descriptor to get its type.
   *
   * @param descriptor The descriptor string to parse, like "I" or "Ljava/lang/String;". See Section
   *     4.3.2 for the definition.
   * @return The type of the field.
   */
  public static char parseFieldDescriptor(final String descriptor) {
    if (descriptor.isEmpty()) {
      throw new IllegalStateException("Descriptor is empty.");
    }

    final var chars = descriptor.toCharArray();
    int i = 0;

    final var type = scanNextFieldType(chars, i);

    if (type.nextStartingIndex != chars.length) {
      throw new IllegalStateException(
          "Descriptor continues past first type: "
              + descriptor
              + " (index "
              + type.nextStartingIndex
              + ")");
    }

    return type.fieldType;
  }

  /**
   * Parses a method descriptor to extract its parameter list and return type.
   *
   * @param descriptor The descriptor string to parse, like "()V" or "(ID[Ljava/lang/Object;)B". See
   *     JVMS Section 4.3.3 for the definition.
   * @return The {@link ParsedMethodDescriptor}.
   */
  public static ParsedMethodDescriptor parseMethodDescriptor(final String descriptor) {
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

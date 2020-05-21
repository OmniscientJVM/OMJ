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
package com.octogonapus.omj.util;

public final class SimpleTypeUtil {

  public enum SimpleType {
    VOID,
    BOOLEAN,
    CHAR,
    BYTE,
    SHORT,
    INT,
    FLOAT,
    LONG,
    DOUBLE,
    REFERENCE;

    public String className;
  }

  public static SimpleType getSimpleTypeFromDescriptorByte(final byte descriptor) {
    switch (descriptor) {
      case 'V':
        return SimpleType.VOID;
      case 'Z':
        return SimpleType.BOOLEAN;
      case 'B':
        return SimpleType.BYTE;
      case 'C':
        return SimpleType.CHAR;
      case 'S':
        return SimpleType.SHORT;
      case 'I':
        return SimpleType.INT;
      case 'F':
        return SimpleType.FLOAT;
      case 'J':
        return SimpleType.LONG;
      case 'D':
        return SimpleType.DOUBLE;
      case 'L':
        return SimpleType.REFERENCE;
      default:
        throw new IllegalArgumentException("Unknown type descriptor byte: " + descriptor);
    }
  }

  /** @return A type descriptor character. */
  public static char getDescriptorChar(final SimpleType type) {
    switch (type) {
      case VOID:
        return 'V';
      case BOOLEAN:
        return 'Z';
      case BYTE:
        return 'B';
      case CHAR:
        return 'C';
      case SHORT:
        return 'S';
      case INT:
        return 'I';
      case FLOAT:
        return 'F';
      case LONG:
        return 'J';
      case DOUBLE:
        return 'D';
      default:
        return 'L';
    }
  }

  /** @return A single-byte type descriptor. */
  public static byte getDescriptorByte(final SimpleType type) {
    return (byte) getDescriptorChar(type);
  }

  /**
   * @return The class name of the {@link SimpleType} where all references (objects and arrays) are
   *     {@code Object}.
   */
  public static String getAdaptedClassName(final SimpleType type) {
    switch (type) {
      case VOID:
        return "void";
      case BOOLEAN:
        return "boolean";
      case CHAR:
        return "char";
      case BYTE:
        return "byte";
      case SHORT:
        return "short";
      case INT:
        return "int";
      case FLOAT:
        return "float";
      case LONG:
        return "long";
      case DOUBLE:
        return "double";
      default:
        return "Object";
    }
  }

  /** @return The number of bytes required to store the type in a trace. */
  public static int getLengthOfTypeForTrace(final SimpleType type) {
    switch (type) {
      case VOID:
        return 0;
      case BOOLEAN:
      case BYTE:
        return 1;
      case CHAR:
      case SHORT:
        return 2;
      case INT:
      case FLOAT:
        return 4;
      case LONG:
      case DOUBLE:
        return 8;
      default:
        // Length of the null-terminated string of the class name plus an int
        return type.className.getBytes().length + 1 + 4;
    }
  }
}

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

import com.octogonapus.omj.agent.parser.Parser;
import org.objectweb.asm.Type;

final class TypeUtil {

  enum SimpleType {
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

    String className;
  }

  /** @return A type descriptor where all references are {@code Ljava/lang/Object;}. */
  static String getAdaptedDescriptor(final Type type) {
    final char shortDesc = Parser.parseFieldDescriptor(type.getDescriptor());
    switch (shortDesc) {
      case 'L':
        return "Ljava/lang/Object;";
      default:
        return "" + shortDesc;
    }
  }

  /** @return A single-byte type descriptor. */
  static byte getDescriptorByte(final Type type) {
    return (byte) Parser.parseFieldDescriptor(type.getDescriptor());
  }

  /** @return A type descriptor character. */
  static char getDescriptorChar(final Type type) {
    return Parser.parseFieldDescriptor(type.getDescriptor());
  }

  /** @return A type descriptor character. */
  static char getDescriptorChar(final SimpleType type) {
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
  static byte getDescriptorByte(final SimpleType type) {
    return (byte) getDescriptorChar(type);
  }

  /** @return The {@link SimpleType} version of the {@link Type}. */
  static SimpleType getSimpleType(final Type type) {
    switch (type.getSort()) {
      case Type.VOID:
        return SimpleType.VOID;
      case Type.BOOLEAN:
        return SimpleType.BOOLEAN;
      case Type.CHAR:
        return SimpleType.CHAR;
      case Type.BYTE:
        return SimpleType.BYTE;
      case Type.SHORT:
        return SimpleType.SHORT;
      case Type.INT:
        return SimpleType.INT;
      case Type.FLOAT:
        return SimpleType.FLOAT;
      case Type.LONG:
        return SimpleType.LONG;
      case Type.DOUBLE:
        return SimpleType.DOUBLE;
      default:
        final SimpleType reference = SimpleType.REFERENCE;
        reference.className = type.getClassName();
        return reference;
    }
  }

  /**
   * @return The class name of the {@link Type} where all references (objects and arrays) are {@code
   *     Object}.
   */
  static String getAdaptedClassName(final Type type) {
    switch (type.getSort()) {
      case Type.VOID:
        return "void";
      case Type.BOOLEAN:
        return "boolean";
      case Type.CHAR:
        return "char";
      case Type.BYTE:
        return "byte";
      case Type.SHORT:
        return "short";
      case Type.INT:
        return "int";
      case Type.FLOAT:
        return "float";
      case Type.LONG:
        return "long";
      case Type.DOUBLE:
        return "double";
      default:
        return "Object";
    }
  }

  /**
   * @return The class name of the {@link SimpleType} where all references (objects and arrays) are
   *     {@code Object}.
   */
  static String getAdaptedClassName(final SimpleType type) {
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
  static int getLengthOfTypeForTrace(final SimpleType type) {
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

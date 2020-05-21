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
import com.octogonapus.omj.util.SimpleTypeUtil;
import org.objectweb.asm.Type;

final class TypeUtil {

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

  /**
   * @return The {@link com.octogonapus.omj.util.SimpleTypeUtil.SimpleType} version of the {@link
   *     Type}.
   */
  static SimpleTypeUtil.SimpleType getSimpleType(final Type type) {
    switch (type.getSort()) {
      case Type.VOID:
        return SimpleTypeUtil.SimpleType.VOID;
      case Type.BOOLEAN:
        return SimpleTypeUtil.SimpleType.BOOLEAN;
      case Type.CHAR:
        return SimpleTypeUtil.SimpleType.CHAR;
      case Type.BYTE:
        return SimpleTypeUtil.SimpleType.BYTE;
      case Type.SHORT:
        return SimpleTypeUtil.SimpleType.SHORT;
      case Type.INT:
        return SimpleTypeUtil.SimpleType.INT;
      case Type.FLOAT:
        return SimpleTypeUtil.SimpleType.FLOAT;
      case Type.LONG:
        return SimpleTypeUtil.SimpleType.LONG;
      case Type.DOUBLE:
        return SimpleTypeUtil.SimpleType.DOUBLE;
      default:
        final SimpleTypeUtil.SimpleType reference = SimpleTypeUtil.SimpleType.REFERENCE;
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
}

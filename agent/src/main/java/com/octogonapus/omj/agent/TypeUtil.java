package com.octogonapus.omj.agent;

import com.octogonapus.omj.agent.parser.Parser;
import org.objectweb.asm.Type;

final class TypeUtil {

  static int getAdaptedSort(final Type type) {
    final int sort = type.getSort();
    switch (sort) {
      case 0:
      case 11:
      case 12:
        throw new IllegalStateException("Cannot handle sort: " + sort);
      case 9:
        return 10;
      default:
        return sort;
    }
  }

  static String getAdaptedDescriptor(final Type type) {
    final char shortDesc = Parser.parseFieldDescriptor(type.getDescriptor());
    switch (shortDesc) {
      case 'L':
        return "Ljava/lang/Object;";
      default:
        return "" + shortDesc;
    }
  }

  static char getShortenedDescriptor(final Type type) {
    return Parser.parseFieldDescriptor(type.getDescriptor());
  }

  static String getClassName(final Type type) {
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

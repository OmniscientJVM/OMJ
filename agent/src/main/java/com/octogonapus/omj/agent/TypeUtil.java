package com.octogonapus.omj.agent;

import com.octogonapus.omj.agent.parser.Parser;
import org.objectweb.asm.Type;

final class TypeUtil {

    static int getAdaptedSort(final Type type) {
        final int sort = type.getSort();
        return switch (sort) {
            case 0, 11, 12 -> throw new IllegalStateException("Cannot handle sort: " + sort);
            case 9 -> 10;
            default -> sort;
        };
    }

    static String getAdaptedDescriptor(final Type type) {
        final char shortDesc = Parser.parseFieldDescriptor(type.getDescriptor());
        return switch (shortDesc) {
            case 'L' -> "Ljava/lang/Object;";
            default -> "" + shortDesc;
        };
    }

    static char getShortenedDescriptor(final Type type) {
        return Parser.parseFieldDescriptor(type.getDescriptor());
    }

    static String getClassName(final Type type) {
        return switch (type.getSort()) {
            case Type.VOID -> "void";
            case Type.BOOLEAN -> "boolean";
            case Type.CHAR -> "char";
            case Type.BYTE -> "byte";
            case Type.SHORT -> "short";
            case Type.INT -> "int";
            case Type.FLOAT -> "float";
            case Type.LONG -> "long";
            case Type.DOUBLE -> "double";
            default -> "Object";
        };
    }
}

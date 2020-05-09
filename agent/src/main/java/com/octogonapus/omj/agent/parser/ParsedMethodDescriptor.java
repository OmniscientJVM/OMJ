package com.octogonapus.omj.agent.parser;

import java.util.List;
import java.util.Objects;

class ParsedMethodDescriptor {

    List<Character> argumentTypes;
    Character returnType;

    ParsedMethodDescriptor(final List<Character> argumentTypes, final Character returnType) {
        this.argumentTypes = argumentTypes;
        this.returnType = returnType;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final ParsedMethodDescriptor that = (ParsedMethodDescriptor) o;
        return Objects.equals(argumentTypes, that.argumentTypes) && Objects.equals(returnType,
                                                                                   that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(argumentTypes, returnType);
    }

    @Override
    public String toString() {
        return "ParsedDescriptor{" + "argumentTypes=" + argumentTypes + ", returnType=" + returnType + '}';
    }
}

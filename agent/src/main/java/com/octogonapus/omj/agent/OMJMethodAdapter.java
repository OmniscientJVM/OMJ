package com.octogonapus.omj.agent;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class OMJMethodAdapter extends MethodVisitor implements Opcodes {

    private final String currentClassName;
    private final String currentClassSource;
    private int currentLineNumber;

    public OMJMethodAdapter(final int api,
                            final MethodVisitor methodVisitor,
                            final String currentClassName,
                            final String currentClassSource) {
        super(api, methodVisitor);
        this.currentClassName = currentClassName;
        this.currentClassSource = currentClassSource;
    }

    @Override
    public void visitMethodInsn(final int opcode,
                                final String owner,
                                final String name,
                                final String descriptor,
                                final boolean isInterface) {
        final var packagePathPrefix = currentClassName.substring(0,
                                                                 currentClassName.lastIndexOf('/') + 1);
        final var packagePrefix = packagePathPrefix.replace('/', '.');
        super.visitLdcInsn(packagePrefix + currentClassSource + ":" + currentLineNumber);
        super.visitMethodInsn(INVOKESTATIC,
                              "com/octogonapus/omj/agentlib/OMJAgentLib",
                              "methodCall",
                              "(Ljava/lang/String;)V",
                              false);

        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
        super.visitLineNumber(line, start);
        currentLineNumber = line;
    }
}

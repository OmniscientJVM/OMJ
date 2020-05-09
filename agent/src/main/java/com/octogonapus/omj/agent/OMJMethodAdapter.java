package com.octogonapus.omj.agent;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

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
        System.out.println("OMJMethodAdapter.visitMethodInsn");
        System.out.println("opcode = " + OpcodeUtil.getNameOfOpcode(opcode) + ", owner = " + owner + ", name = " + name + ", " + "descriptor = " + descriptor + ", isInterface = " + isInterface);

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

    @Override
    public void visitInsn(final int opcode) {
        System.out.println("OMJMethodAdapter.visitInsn");
        System.out.println("opcode = " + OpcodeUtil.getNameOfOpcode(opcode));
        super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        System.out.println("OMJMethodAdapter.visitIntInsn");
        System.out.println("opcode = " + OpcodeUtil.getNameOfOpcode(opcode) + ", operand = " + operand);
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        System.out.println("OMJMethodAdapter.visitVarInsn");
        System.out.println("opcode = " + OpcodeUtil.getNameOfOpcode(opcode) + ", var = " + var);
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitLdcInsn(final Object value) {
        System.out.println("OMJMethodAdapter.visitLdcInsn");
        System.out.println("value = " + value);
        super.visitLdcInsn(value);
    }

    @Override
    public void visitFrame(final int type,
                           final int numLocal,
                           final Object[] local,
                           final int numStack,
                           final Object[] stack) {
        System.out.println("OMJMethodAdapter.visitFrame");
        System.out.println("type = " + type + ", numLocal = " + numLocal + ", local = " + Arrays.deepToString(
                local) + ", numStack = " + numStack + ", stack = " + Arrays.deepToString(stack));
        super.visitFrame(type, numLocal, local, numStack, stack);
    }

    @Override
    public void visitParameter(final String name, final int access) {
        System.out.println("OMJMethodAdapter.visitParameter");
        System.out.println("name = " + name + ", access = " + access);
        super.visitParameter(name, access);
    }

    @Override
    public void visitFieldInsn(final int opcode,
                               final String owner,
                               final String name,
                               final String descriptor) {
        System.out.println("OMJMethodAdapter.visitFieldInsn");
        System.out.println("opcode = " + OpcodeUtil.getNameOfOpcode(opcode) + ", owner = " + owner + ", name = " + name + ", " + "descriptor = " + descriptor);
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        System.out.println("OMJMethodAdapter.visitIincInsn");
        System.out.println("var = " + var + ", increment = " + increment);
        super.visitIincInsn(var, increment);
    }
}

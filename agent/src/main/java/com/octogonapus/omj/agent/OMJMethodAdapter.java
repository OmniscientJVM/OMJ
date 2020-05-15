package com.octogonapus.omj.agent;

import java.util.Arrays;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public final class OMJMethodAdapter extends MethodVisitor implements Opcodes {

  private final DynamicClassDefiner dynamicClassDefiner;
  private final String currentClassSource;
  private final String methodDescriptor;
  private int currentLineNumber;
  private final String packagePrefix;
  private final boolean isStatic;

  public OMJMethodAdapter(
      final int api,
      final MethodVisitor methodVisitor,
      final DynamicClassDefiner dynamicClassDefiner,
      final String currentClassName,
      final String currentClassSource,
      final String methodDescriptor,
      final boolean isStatic) {
    super(api, methodVisitor);
    this.dynamicClassDefiner = dynamicClassDefiner;
    this.currentClassSource = currentClassSource;
    this.methodDescriptor = methodDescriptor;
    packagePrefix =
        currentClassName.substring(0, currentClassName.lastIndexOf('/') + 1).replace('/', '.');
    this.isStatic = isStatic;
  }

  @Override
  public void visitCode() {
    super.visitCode();

    final String dynamicClassName = dynamicClassDefiner.defineClassForMethod(methodDescriptor);

    super.visitTypeInsn(NEW, dynamicClassName);
    super.visitInsn(DUP);
    super.visitMethodInsn(INVOKESPECIAL, dynamicClassName, "<init>", "()V", false);
    super.visitMethodInsn(
        INVOKESTATIC,
        "com/octogonapus/omj/agentlib/OMJAgentLib",
        "methodCall_start",
        "(Lcom/octogonapus/omj/agentlib/MethodTrace;)V",
        false);

    final Type[] argumentTypes = Type.getArgumentTypes(methodDescriptor);
    System.out.println("argumentTypes = " + Arrays.toString(argumentTypes));

    final int virtualOffset;

    if (!isStatic) {
      super.visitVarInsn(ALOAD, 0);
      super.visitMethodInsn(
          INVOKESTATIC,
          "com/octogonapus/omj/agentlib/OMJAgentLib",
          "methodCall_argument_Object",
          "(Ljava/lang/Object;)V",
          false);
      virtualOffset = 1;
    } else {
      virtualOffset = 0;
    }

    for (int i = 0; i < argumentTypes.length; i++) {
      final Type argumentType = argumentTypes[i];

      final String methodName = "methodCall_argument_" + TypeUtil.getClassName(argumentType);
      final String methodDesc = "(" + TypeUtil.getAdaptedDescriptor(argumentType) + ")V";
      System.out.println("Generated methodName = " + methodName);
      System.out.println("Generated methodDesc = " + methodDesc);

      super.visitVarInsn(argumentType.getOpcode(ILOAD), i + virtualOffset);
      super.visitMethodInsn(
          INVOKESTATIC, "com/octogonapus/omj/agentlib/OMJAgentLib", methodName, methodDesc, false);
    }

    super.visitMethodInsn(
        INVOKESTATIC, "com/octogonapus/omj/agentlib/OMJAgentLib", "methodCall_end", "()V", false);
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    super.visitLineNumber(line, start);
    currentLineNumber = line;
  }

  @Override
  public void visitVarInsn(final int opcode, final int var) {
    super.visitVarInsn(opcode, var);
  }
}

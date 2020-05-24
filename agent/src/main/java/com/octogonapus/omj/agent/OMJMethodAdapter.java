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

import java.util.Arrays;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OMJMethodAdapter extends MethodVisitor implements Opcodes {

  private final Logger logger = LoggerFactory.getLogger(OMJMethodAdapter.class);
  private final DynamicClassDefiner dynamicClassDefiner;
  private final String methodDescriptor;
  private final boolean isStatic;
  private final String fullyQualifiedClassName;
  private int currentLineNumber;

  public OMJMethodAdapter(
      final int api,
      final MethodVisitor methodVisitor,
      final DynamicClassDefiner dynamicClassDefiner,
      final String methodDescriptor,
      final boolean isStatic,
      final String currentClassName) {
    super(api, methodVisitor);
    this.dynamicClassDefiner = dynamicClassDefiner;
    this.methodDescriptor = methodDescriptor;
    this.isStatic = isStatic;

    final int indexOfLastSeparator = currentClassName.lastIndexOf('/') + 1;
    final String packagePrefix = currentClassName.substring(0, indexOfLastSeparator);
    final String className = currentClassName.substring(indexOfLastSeparator);
    this.fullyQualifiedClassName = packagePrefix.replace('/', '.') + className;
  }

  @Override
  public void visitCode() {
    super.visitCode();

    final String dynamicClassName =
        dynamicClassDefiner.defineClassForMethod(methodDescriptor, isStatic);

    // Make a new instance of the dynamic class we just generated. Pass the method location to it so
    // that this method can be identified later on. Then start pass the initialized instance to the
    // agent lib.
    super.visitTypeInsn(NEW, dynamicClassName);
    super.visitInsn(DUP);
    if (isStatic) {
      super.visitInsn(ICONST_1);
    } else {
      super.visitInsn(ICONST_0);
    }
    super.visitMethodInsn(INVOKESPECIAL, dynamicClassName, "<init>", "(Z)V", false);
    super.visitMethodInsn(
        INVOKESTATIC,
        "com/octogonapus/omj/agentlib/OMJAgentLib",
        "methodCall_start",
        "(Lcom/octogonapus/omj/agentlib/MethodTrace;)V",
        false);

    final Type[] argumentTypes = Type.getArgumentTypes(methodDescriptor);
    logger.debug("argumentTypes = " + Arrays.toString(argumentTypes));

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

      final String methodName = "methodCall_argument_" + TypeUtil.getAdaptedClassName(argumentType);
      final String methodDesc = "(" + TypeUtil.getAdaptedDescriptor(argumentType) + ")V";
      logger.debug("Generated methodName = " + methodName);
      logger.debug("Generated methodDesc = " + methodDesc);

      super.visitVarInsn(argumentType.getOpcode(ILOAD), i + virtualOffset);
      super.visitMethodInsn(
          INVOKESTATIC, "com/octogonapus/omj/agentlib/OMJAgentLib", methodName, methodDesc, false);
    }

    super.visitMethodInsn(
        INVOKESTATIC, "com/octogonapus/omj/agentlib/OMJAgentLib", "methodCall_end", "()V", false);
  }

  @Override
  public void visitMethodInsn(
      final int opcode,
      final String owner,
      final String name,
      final String descriptor,
      final boolean isInterface) {
    // Record the line number before the method call so that the trace container will get the
    // correct line number
    if (opcode == INVOKEVIRTUAL || opcode == INVOKESTATIC) {
      super.visitLdcInsn(fullyQualifiedClassName);
      super.visitMethodInsn(
          INVOKESTATIC,
          "com/octogonapus/omj/agentlib/OMJAgentLib",
          "className",
          "(Ljava/lang/String;)V",
          false);
      super.visitLdcInsn(currentLineNumber);
      super.visitMethodInsn(
          INVOKESTATIC, "com/octogonapus/omj/agentlib/OMJAgentLib", "lineNumber", "(I)V", false);
      super.visitLdcInsn(name);
      super.visitMethodInsn(
          INVOKESTATIC,
          "com/octogonapus/omj/agentlib/OMJAgentLib",
          "methodName",
          "(Ljava/lang/String;)V",
          false);
    }

    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    super.visitLineNumber(line, start);
    currentLineNumber = line;
  }
}

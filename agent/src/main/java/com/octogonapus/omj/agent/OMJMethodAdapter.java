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
    final String methodLocation = packagePrefix + currentClassSource + ":" + currentLineNumber;

    // Make a new instance of the dynamic class we just generated. Pass the method location to it so
    // that this method can be identified later on. Then start pass the initialized instance to the
    // agent lib.
    super.visitTypeInsn(NEW, dynamicClassName);
    super.visitInsn(DUP);
    super.visitLdcInsn(methodLocation);
    super.visitMethodInsn(
        INVOKESPECIAL, dynamicClassName, "<init>", "(Ljava/lang/String;)V", false);
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
  public void visitLineNumber(final int line, final Label start) {
    super.visitLineNumber(line, start);
    currentLineNumber = line;
  }

  @Override
  public void visitVarInsn(final int opcode, final int var) {
    super.visitVarInsn(opcode, var);
  }
}

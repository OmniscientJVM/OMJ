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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

class MethodAdapterUtil {

  /**
   * Records contextual information about a method call. This should be used before the method call
   * happens, when visiting method instructions.
   *
   * @param methodVisitor The method visitor to delegate to.
   * @param currentLineNumber The most up-to-date line number from the method visitor.
   * @param fullyQualifiedClassName The fully-qualified name of the class the method call happens
   *     in.
   * @param name The name of the method.
   */
  static void visitMethodCallStartPreamble(
      final MethodVisitor methodVisitor,
      final int currentLineNumber,
      final String fullyQualifiedClassName,
      final String name) {
    methodVisitor.visitLdcInsn(fullyQualifiedClassName);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "com/octogonapus/omj/agentlib/OMJAgentLib",
        "className",
        "(Ljava/lang/String;)V",
        false);

    methodVisitor.visitLdcInsn(currentLineNumber);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "com/octogonapus/omj/agentlib/OMJAgentLib",
        "lineNumber",
        "(I)V",
        false);

    methodVisitor.visitLdcInsn(name);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "com/octogonapus/omj/agentlib/OMJAgentLib",
        "methodName",
        "(Ljava/lang/String;)V",
        false);
  }

  /**
   * Generates a method trace container class, makes a new instance of it, records all of the
   * method's arguments, and gives the trace to the agent-lib.
   *
   * @param methodVisitor The method visitor to delegate to.
   * @param methodDescriptor The method's descriptor.
   * @param isStatic True if the method is static.
   * @param dynamicClassDefiner The {@link DynamicClassDefiner} to use to generate the method trace
   *     container class.
   * @param logger Used to log debug information.
   */
  static void recordMethodTrace(
      final MethodVisitor methodVisitor,
      final String methodDescriptor,
      final boolean isStatic,
      final DynamicClassDefiner dynamicClassDefiner,
      final Logger logger) {
    // Generate a method trace container class and make a new instance of it. Contextual information
    // about the is passed to the agent lib earlier when method instructions are visited using
    // `visitMethodCallStartPreamble`.
    final String dynamicClassName =
        dynamicClassDefiner.defineClassForMethod(methodDescriptor, isStatic);

    methodVisitor.visitTypeInsn(Opcodes.NEW, dynamicClassName);
    methodVisitor.visitInsn(Opcodes.DUP);
    methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, dynamicClassName, "<init>", "()V", false);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "com/octogonapus/omj/agentlib/OMJAgentLib",
        "methodCall_start",
        "(Lcom/octogonapus/omj/agentlib/MethodTrace;)V",
        false);

    final Type[] argumentTypes = Type.getArgumentTypes(methodDescriptor);
    logger.debug("argumentTypes = " + Arrays.toString(argumentTypes));

    final int virtualOffset;

    if (!isStatic) {
      methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
      methodVisitor.visitMethodInsn(
          Opcodes.INVOKESTATIC,
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

      methodVisitor.visitVarInsn(argumentType.getOpcode(Opcodes.ILOAD), i + virtualOffset);
      methodVisitor.visitMethodInsn(
          Opcodes.INVOKESTATIC,
          "com/octogonapus/omj/agentlib/OMJAgentLib",
          methodName,
          methodDesc,
          false);
    }

    methodVisitor.visitMethodInsn(
        Opcodes.INVOKESTATIC,
        "com/octogonapus/omj/agentlib/OMJAgentLib",
        "methodCall_end",
        "()V",
        false);
  }
}

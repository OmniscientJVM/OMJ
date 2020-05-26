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
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OMJClassAdapter extends ClassVisitor implements Opcodes {

  private final Logger logger = LoggerFactory.getLogger(OMJClassAdapter.class);
  private final DynamicClassDefiner dynamicClassDefiner;
  private int classVersion;
  private String className;
  private String superName;

  public OMJClassAdapter(
      final int api,
      final ClassVisitor classVisitor,
      final DynamicClassDefiner dynamicClassDefiner) {
    super(api, classVisitor);
    this.dynamicClassDefiner = dynamicClassDefiner;
  }

  @Override
  public void visit(
      final int version,
      final int access,
      final String name,
      final String signature,
      final String superName,
      final String[] interfaces) {
    logger.debug(
        "version = {}, access = {}, name = {}, signature = {}, superName = {}, interfaces = {}",
        version,
        access,
        name,
        signature,
        superName,
        Arrays.deepToString(interfaces));

    super.visit(version, access, name, signature, superName, interfaces);
    classVersion = version;
    className = name;
    this.superName = superName;
  }

  @Override
  public MethodVisitor visitMethod(
      final int access,
      final String name,
      final String descriptor,
      final String signature,
      final String[] exceptions) {
    logger.debug(
        "access = {}, name = {}, descriptor = {}, signature = {}, exceptions = {}",
        access,
        name,
        descriptor,
        signature,
        Arrays.deepToString(exceptions));

    final var visitor = super.visitMethod(access, name, descriptor, signature, exceptions);

    if (isInstanceInitializationMethod(name, descriptor)) {
      return new OMJInstanceInitializationMethodAdapter(
          api, visitor, dynamicClassDefiner, descriptor, className, superName);
    } else if (isClassInitializationMethod(name, descriptor, access)) {
      return new OMJMethodAdapter(
          api, visitor, dynamicClassDefiner, descriptor, isStatic(access), className);
    } else {
      return new OMJMethodAdapter(
          api, visitor, dynamicClassDefiner, descriptor, isStatic(access), className);
    }
  }

  private boolean isStatic(final int access) {
    return (access & ACC_STATIC) == ACC_STATIC;
  }

  /**
   * Determines whether the method is an instance initialization method according to Section 2.9.1.
   *
   * @param methodName The method's name.
   * @param descriptor The method's descriptor.
   * @return True if the method is an instance initialization method.
   */
  private boolean isInstanceInitializationMethod(final String methodName, final String descriptor) {
    return methodName.equals("<init>") && Type.getReturnType(descriptor).getSort() == Type.VOID;
  }

  /**
   * Determines whether the method is an class initialization method according to Section 2.9.2.
   *
   * @param methodName The method's name.
   * @param descriptor The method's descriptor.
   * @return True if the method is an class initialization method.
   */
  private boolean isClassInitializationMethod(
      final String methodName, final String descriptor, final int access) {
    final int majorVersion = classVersion & 0xFFFF;

    final boolean versionCheck;
    if (majorVersion >= 51) {
      versionCheck = isStatic(access) && Type.getArgumentTypes(descriptor).length == 0;
    } else {
      versionCheck = true;
    }

    return methodName.equals("<clinit>")
        && Type.getReturnType(descriptor).getSort() == Type.VOID
        && versionCheck;
  }
}

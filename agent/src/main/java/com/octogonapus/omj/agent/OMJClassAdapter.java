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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OMJClassAdapter extends ClassVisitor implements Opcodes {

  private final Logger logger = LoggerFactory.getLogger(OMJClassAdapter.class);
  private final DynamicClassDefiner dynamicClassDefiner;
  private String currentClassName;

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
    super.visit(version, access, name, signature, superName, interfaces);
    currentClassName = name;
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

    if (!name.equals("<init>") && !name.equals("<clinit>")) {
      // TODO: Handle init and clinit. Need to grab the this pointer after the superclass
      //  ctor is called.

      logger.debug("Adapting method.");
      final var isStatic = (access & ACC_STATIC) == ACC_STATIC;

      return new OMJMethodAdapter(
          api, visitor, dynamicClassDefiner, descriptor, isStatic, currentClassName);
    } else {
      return visitor;
    }
  }
}

package com.octogonapus.omj.agent;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;

public class OMJClassAdapter extends ClassVisitor implements Opcodes {

    private String currentClassName;
    private String currentClassSource;

    public OMJClassAdapter(final int api, final ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String signature,
                      final String superName,
                      final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        currentClassName = name;
    }

    @Override
    public MethodVisitor visitMethod(final int access,
                                     final String name,
                                     final String descriptor,
                                     final String signature,
                                     final String[] exceptions) {
        System.out.println("OMJClassAdapter.visitMethod");
        System.out.println(
                "access = " + access + ", name = " + name + ", descriptor = " + descriptor +
                ", signature = " + signature + ", exceptions = " + Arrays.deepToString(exceptions));
        final var result = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (result == null) {
            return null;
        } else if (!name.equals("<init>") && !name.equals("<clinit>")) {
            System.out.println("ADAPTING METHOD");
            // TODO: Handle init and clinit. Need to grab the this pointer after the superclass
            //  ctor is called.
            final var isStatic = (access & ACC_STATIC) == ACC_STATIC;
            return new OMJMethodAdapter(api,
                                        result,
                                        currentClassName,
                                        currentClassSource,
                                        name,
                                        descriptor,
                                        signature,
                                        isStatic);
        } else {
            return result;
        }
    }

    @Override
    public void visitSource(final String source, final String debug) {
        super.visitSource(source, debug);
        currentClassSource = source;
    }
}

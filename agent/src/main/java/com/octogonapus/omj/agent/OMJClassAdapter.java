package com.octogonapus.omj.agent;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public class OMJClassAdapter extends ClassVisitor implements Opcodes {

    private final Pattern classFilter;
    private boolean shouldAdapt = false;
    private String currentClassName;
    private String currentClassSource;

    public OMJClassAdapter(final int api,
                           final ClassVisitor classVisitor,
                           final Pattern classFilter) {
        super(api, classVisitor);
        this.classFilter = classFilter;
    }

    @Override
    public void visit(final int version,
                      final int access,
                      final String name,
                      final String signature,
                      final String superName,
                      final String[] interfaces) {
        // Only adapt in this package
        shouldAdapt = classFilter.matcher(name).matches();
        currentClassName = name;

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(final int access,
                                     final String name,
                                     final String descriptor,
                                     final String signature,
                                     final String[] exceptions) {
        System.out.println("OMJClassAdapter.visitMethod");
        System.out.println("access = " + access + ", name = " + name + ", descriptor = " + descriptor + ", signature = " + signature + ", exceptions = " + Arrays
                .deepToString(exceptions));
        final var result = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (result == null) {
            return null;
        } else if (shouldAdapt && !name.equals("<init>") && !name.equals("<clinit>")) {
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

package com.octogonapus.omj.agent;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class OMJClassFileTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(final ClassLoader loader,
                            final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer) {
        System.out.println("className = " + className);
        return transformClassBytes(classfileBuffer);
    }

    public static byte[] transformClassBytes(final byte[] classfileBuffer) {
        final var classReader = new ClassReader(classfileBuffer);
        final var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final var trace = new TraceClassVisitor(classWriter, new PrintWriter(System.out));
        classReader.accept(new OMJClassAdapter(Opcodes.ASM8,
                                               trace,
                                               Pattern.compile("com/octogonapus/[a-zA-Z]*")), 0);
        return classWriter.toByteArray();
    }
}

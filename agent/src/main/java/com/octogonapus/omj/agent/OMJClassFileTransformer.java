package com.octogonapus.omj.agent;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.regex.Pattern;

public class OMJClassFileTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(final ClassLoader loader,
                            final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer) {
        System.out.println("OMJClassFileTransformer.transform");
        System.out.println("loader = " + loader + ", className = " + className + ", " +
                           "classBeingRedefined = " + classBeingRedefined + ", " +
                           "protectionDomain = " + protectionDomain + ", classfileBuffer " + "= " +
                           Arrays.toString(classfileBuffer));

        final Pattern includeFilter = Pattern.compile("com/octogonapus/[a-zA-Z]*");
        final Pattern excludeFilter = Pattern.compile("com/octogonapus/omj/[a-zA-Z]*");
        final boolean shouldAdapt = includeFilter.matcher(className).matches() &&
                                    !excludeFilter.matcher(className).matches();

        if (shouldAdapt) {
            try {
                // If transformClassBytes throws an exception, then the class will silently not
                // be transformed. This is very hard to debug, so catch anything it throws and
                // explode.
                return transformClassBytes(classfileBuffer);
            } catch (Throwable ex) {
                ex.printStackTrace();
                System.exit(1);
                return null;
            }
        } else {
            return null;
        }
    }

    public static byte[] transformClassBytes(final byte[] classfileBuffer) {
        final var classReader = new ClassReader(classfileBuffer);
        final var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final var trace = new TraceClassVisitor(classWriter, new PrintWriter(System.out));
        classReader.accept(new OMJClassAdapter(Opcodes.ASM8, trace), 0);
        return classWriter.toByteArray();
    }
}

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class OMJClassFileTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(final ClassLoader loader,
                            final String className,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] classfileBuffer) {
        final var classReader = new ClassReader(classfileBuffer);
        final var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classReader.accept(new OMJClassAdapter(Opcodes.ASM8,
                                               classWriter,
                                               Pattern.compile("com/octogonapus/[a-zA-Z]*")), 0);
        return classWriter.toByteArray();
    }
}

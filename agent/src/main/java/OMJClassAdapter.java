import java.util.regex.Pattern;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
        final var result = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (result == null) {
            return null;
        } else if (shouldAdapt) {
            return new OMJMethodAdapter(api, result, currentClassName, currentClassSource);
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

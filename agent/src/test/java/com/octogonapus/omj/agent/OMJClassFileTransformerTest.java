package com.octogonapus.omj.agent;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

class ByteArrayClassLoader extends ClassLoader {

    private final byte[] bytes;

    public ByteArrayClassLoader(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        if (name.equals("DynamicFoo")) {
            return defineClass(name, bytes, 0, bytes.length);
        }

        return super.findClass(name);
    }
}

class OMJClassFileTransformerTest {

    @Test
    @Disabled
    void testSimpleMethodCall() throws
            ClassNotFoundException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
        final var classBytes =
                new ByteBuddy().subclass(Object.class)
                        .name("DynamicFoo")
                        .defineMethod("foo",
                                      Void.class,
                                      Modifier.PUBLIC)
                        .intercept(MethodCall.run(() -> {
                            System.out.println("hello from new class");
                        }))
                        .make()
                        .getBytes();

        final var newBytes = OMJClassFileTransformer.transformClassBytes(classBytes);

        final var newClass = new ByteArrayClassLoader(newBytes).loadClass("DynamicFoo");

        newClass.getDeclaredMethod("foo").invoke(newClass.getConstructor().newInstance());


        //                .load(OMJClassFileTransformer.class.getClassLoader())
        //                .getLoaded();

        //        try {
        //            classBytes.getDeclaredMethod("foo").invoke(classBytes.getConstructor()
        //            .newInstance());
        //        } catch (NoSuchMethodException e) {
        //            e.printStackTrace();
        //        } catch (IllegalAccessException e) {
        //            e.printStackTrace();
        //        } catch (InstantiationException e) {
        //            e.printStackTrace();
        //        } catch (InvocationTargetException e) {
        //            e.printStackTrace();
        //        }
    }
}

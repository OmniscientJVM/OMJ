package com.octogonapus.omj.agentlib;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
final public class OMJAgentLib {

    static {
        System.out.println("OMJ agent-lib loaded.");
    }

    private static final AtomicInteger methodCounter = new AtomicInteger(0);
    private static final ThreadLocal<MethodTrace> currentMethodTrace = new ThreadLocal<>();
    private static final ConcurrentLinkedQueue<MethodTrace> methodTraceQueue =
            new ConcurrentLinkedQueue<>();

    public static void methodCall_start(final MethodTrace methodTrace) {
        System.out.println("OMJAgentLib.methodCall_start");
        currentMethodTrace.set(methodTrace);
    }

    public static void methodCall_end() {
        final MethodTrace trace = currentMethodTrace.get();
        currentMethodTrace.remove();
        methodTraceQueue.add(trace);
    }

    public static void methodCall_argument_boolean(boolean value) {
        currentMethodTrace.get().set_argument_boolean(value);
    }

    public static void methodCall_argument_char(char value) {
        currentMethodTrace.get().set_argument_char(value);
    }

    public static void methodCall_argument_byte(byte value) {
        currentMethodTrace.get().set_argument_byte(value);
    }

    public static void methodCall_argument_short(short value) {
        currentMethodTrace.get().set_argument_short(value);
    }

    public static void methodCall_argument_int(int value) {
        currentMethodTrace.get().set_argument_int(value);
    }

    public static void methodCall_argument_float(float value) {
        currentMethodTrace.get().set_argument_float(value);
    }

    public static void methodCall_argument_long(long value) {
        currentMethodTrace.get().set_argument_long(value);
    }

    public static void methodCall_argument_double(double value) {
        currentMethodTrace.get().set_argument_double(value);
    }

    public static void methodCall_argument_Object(Object value) {
        currentMethodTrace.get().set_argument_Object(value);
    }
}

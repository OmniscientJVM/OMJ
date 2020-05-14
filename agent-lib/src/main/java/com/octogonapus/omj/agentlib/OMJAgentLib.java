package com.octogonapus.omj.agentlib;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

final public class OMJAgentLib {

    static {
        System.out.println("OMJ agent-lib loaded.");
    }

    private static final AtomicInteger methodCounter = new AtomicInteger(0);
    private static final ThreadLocal<MethodTrace> currentMethodTrace = new ThreadLocal<>();
    private static final ConcurrentLinkedQueue<MethodTrace> methodTraceQueue =
            new ConcurrentLinkedQueue<>();

    public static void methodCall_start(MethodTrace methodTrace) {
        System.out.println("OMJAgentLib.methodCall_start");
        currentMethodTrace.set(methodTrace);
    }

    public static void methodCall_end() {
        final MethodTrace trace = currentMethodTrace.get();
        currentMethodTrace.remove();
        methodTraceQueue.add(trace);
    }

    public static void methodCall_argument_1(boolean value) {
        currentMethodTrace.get().set_argument_1(value);
    }

    public static void methodCall_argument_2(char value) {
        currentMethodTrace.get().set_argument_2(value);
    }

    public static void methodCall_argument_3(byte value) {
        currentMethodTrace.get().set_argument_3(value);
    }

    public static void methodCall_argument_4(short value) {
        currentMethodTrace.get().set_argument_4(value);
    }

    public static void methodCall_argument_5(int value) {
        currentMethodTrace.get().set_argument_5(value);
    }

    public static void methodCall_argument_6(float value) {
        currentMethodTrace.get().set_argument_6(value);
    }

    public static void methodCall_argument_7(long value) {
        currentMethodTrace.get().set_argument_7(value);
    }

    public static void methodCall_argument_8(double value) {
        currentMethodTrace.get().set_argument_8(value);
    }

    public static void methodCall_argument_10(Object value) {
        currentMethodTrace.get().set_argument_10(value);
    }
}

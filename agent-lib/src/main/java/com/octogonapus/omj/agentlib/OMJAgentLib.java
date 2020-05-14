package com.octogonapus.omj.agentlib;

import java.util.concurrent.atomic.AtomicInteger;

public class OMJAgentLib {

    static {
        System.out.println("OMJ agent-lib loaded.");
    }

    private static final AtomicInteger methodCounter = new AtomicInteger(0);
    private static final ThreadLocal<String> currentMethodIdentifier = new ThreadLocal<>();

    public static void methodCall_start(String methodIdentifier) {
        currentMethodIdentifier.set(methodIdentifier);
        System.out.println("OMJAgentLib.methodCall_start");
        System.out.println("Method call start (" + methodCounter.incrementAndGet() + "): " + methodIdentifier);
    }

    public static void methodCall_end() {
        System.out.println("OMJAgentLib.methodCall_end");
        System.out.println("Method call end: " + currentMethodIdentifier.get());
    }

    public static void methodCall_argument_1(boolean value) {
        System.out.println("OMJAgentLib.methodCall_argument_1");
        System.out.println("value = " + value);
    }

    public static void methodCall_argument_2(char value) {
        System.out.println("OMJAgentLib.methodCall_argument_2");
        System.out.println("value = " + value);
    }

    public static void methodCall_argument_3(byte value) {
        System.out.println("OMJAgentLib.methodCall_argument_3");
        System.out.println("value = " + value);
    }

    public static void methodCall_argument_4(short value) {
        System.out.println("OMJAgentLib.methodCall_argument_4");
        System.out.println("value = " + value);
    }

    public static void methodCall_argument_5(int value) {
        System.out.println("OMJAgentLib.methodCall_argument_5");
        System.out.println("value = " + value);
    }

    public static void methodCall_argument_6(float value) {
        System.out.println("OMJAgentLib.methodCall_argument_6");
        System.out.println("value = " + value);
    }

    public static void methodCall_argument_7(long value) {
        System.out.println("OMJAgentLib.methodCall_argument_7");
        System.out.println("value = " + value);
    }

    public static void methodCall_argument_8(double value) {
        System.out.println("OMJAgentLib.methodCall_argument_8");
        System.out.println("value = " + value);
    }

    public static void methodCall_argument_10(Object value) {
        System.out.println("OMJAgentLib.methodCall_argument_10");
        System.out.println("value = " + value);
    }
}

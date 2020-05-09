package com.octogonapus.omj.agentlib;

import java.util.concurrent.atomic.AtomicInteger;

public class OMJAgentLib {

    static {
        System.out.println("OMJ agent-lib loaded.");
    }

    private static AtomicInteger methodCounter = new AtomicInteger(0);

    public static void methodCall(String methodName) {
        System.out.println("Method call (" + methodCounter.incrementAndGet() + "): " + methodName);
    }
}

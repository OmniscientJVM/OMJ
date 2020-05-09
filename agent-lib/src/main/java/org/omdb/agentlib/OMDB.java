package org.omdb.agentlib;

import java.util.concurrent.atomic.AtomicInteger;

public class OMDB {

    static {
        System.out.println("OMDB loaded.");
    }

    private static AtomicInteger methodCounter = new AtomicInteger(0);

    public static void methodCall(String methodName) {
        System.out.println("Method call (" + methodCounter.incrementAndGet() + "): " + methodName);
    }
}

package com.octogonapus.omj.agentlib;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
final public class OMJAgentLib {

    private static final AtomicInteger methodCounter = new AtomicInteger(0);
    private static final ThreadLocal<MethodTrace> currentMethodTrace = new ThreadLocal<>();
    private static final ConcurrentLinkedQueue<MethodTrace> methodTraceQueue =
            new ConcurrentLinkedQueue<>();

    static {
        System.out.println("OMJ agent-lib loaded.");

        final Semaphore traceProcessorRunning = new Semaphore(1);

        final var traceProcessorThread = new Thread(() -> {
            traceProcessorRunning.acquireUninterruptibly();

            while (!Thread.currentThread().isInterrupted()) {
                final var methodTrace = methodTraceQueue.poll();
                if (methodTrace != null) {
                    System.out.println("methodTrace = " + methodTrace);
                }

                // TODO: Don't busy-wait here
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            traceProcessorRunning.release();
        });

        traceProcessorThread.setDaemon(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            traceProcessorThread.interrupt();
            // Wait for the trace processor to finish
            traceProcessorRunning.acquireUninterruptibly();
        }));

        traceProcessorThread.start();
    }

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

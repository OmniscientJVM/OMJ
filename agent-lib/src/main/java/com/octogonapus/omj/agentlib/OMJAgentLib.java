/*
 * This file is part of OMJ.
 *
 * OMJ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OMJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OMJ.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.octogonapus.omj.agentlib;

import com.octogonapus.omj.util.Util;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public final class OMJAgentLib {

  private static final Logger logger = LoggerFactory.getLogger(OMJAgentLib.class);
  private static final AtomicLong traceCounter = new AtomicLong(0);
  private static final ThreadLocal<MethodTrace> currentMethodTrace = new ThreadLocal<>();
  private static final ThreadLocal<String> currentClassName = ThreadLocal.withInitial(() -> "");
  private static final ThreadLocal<Integer> currentLineNumber = ThreadLocal.withInitial(() -> 0);
  private static final ThreadLocal<String> currentMethodName = ThreadLocal.withInitial(() -> "");
  private static final ConcurrentLinkedQueue<Trace> traceQueue = new ConcurrentLinkedQueue<>();
  private static volatile boolean finishProcessingTraces = false;

  static {
    final Semaphore traceProcessorRunning = new Semaphore(1);

    final var traceProcessorThread =
        new Thread(
            () -> {
              traceProcessorRunning.acquireUninterruptibly();

              // TODO: This should probably use a memory-mapped file
              final var traceFile =
                  Util.getTraceDir().resolve("trace_" + System.currentTimeMillis() + ".trace");
              logger.debug("Opening trace file {}", traceFile.toString());
              try (final var os = new BufferedOutputStream(Files.newOutputStream(traceFile))) {
                loopWriteTraces(os);
                logger.debug(
                    "Number of traces left in the queue when flushing: {}", traceQueue.size());
                os.flush();
              } catch (IOException e) {
                e.printStackTrace();
                logger.error("Failed to write traces.", e);

                // Need to release this here because System.exit does not return, so this is our
                // only chance to release the semaphore for the shutdown hook that will run later.
                traceProcessorRunning.release();
                System.exit(1);
              }

              traceProcessorRunning.release();
            });

    traceProcessorThread.setDaemon(true);

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  finishProcessingTraces = true;

                  // Wait for the trace processor to finish so data is flushed out
                  traceProcessorRunning.acquireUninterruptibly();
                }));

    traceProcessorThread.start();
  }

  private static void loopWriteTraces(final OutputStream os) {
    while (!finishProcessingTraces) {
      final var trace = traceQueue.poll();
      if (trace != null) {
        try {
          trace.serialize(os);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      if (traceQueue.isEmpty()) {
        // Only wait if there are no more traces to process
        // TODO: Don't busy-wait here
        try {
          Thread.sleep(1);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }

    traceQueue.forEach(
        trace -> {
          try {
            trace.serialize(os);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  public static void className(final String className) {
    currentClassName.set(className);
  }

  public static void lineNumber(final int lineNumber) {
    currentLineNumber.set(lineNumber);
  }

  public static void methodName(final String methodName) {
    currentMethodName.set(methodName);
  }

  public static void methodCall_start(final MethodTrace methodTrace) {
    methodTrace.setIndex(traceCounter.getAndIncrement());
    methodTrace.setClassName(currentClassName.get());
    methodTrace.setLineNumber(currentLineNumber.get());
    methodTrace.setMethodName(currentMethodName.get());
    currentMethodTrace.set(methodTrace);
  }

  public static void methodCall_end() {
    final MethodTrace trace = currentMethodTrace.get();
    currentMethodTrace.remove();
    traceQueue.add(trace);
  }

  public static void store(
      final boolean value,
      final String className,
      final int lineNumber,
      final String variableName) {
    final var trace =
        new StoreTrace_boolean(
            traceCounter.getAndIncrement(), className, lineNumber, variableName, value);
    traceQueue.add(trace);
  }

  public static void store(
      final char value, final String className, final int lineNumber, final String variableName) {
    final var trace =
        new StoreTrace_char(
            traceCounter.getAndIncrement(), className, lineNumber, variableName, value);
    traceQueue.add(trace);
  }

  public static void store(
      final byte value, final String className, final int lineNumber, final String variableName) {
    final var trace =
        new StoreTrace_byte(
            traceCounter.getAndIncrement(), className, lineNumber, variableName, value);
    traceQueue.add(trace);
  }

  public static void store(
      final short value, final String className, final int lineNumber, final String variableName) {
    final var trace =
        new StoreTrace_short(
            traceCounter.getAndIncrement(), className, lineNumber, variableName, value);
    traceQueue.add(trace);
  }

  public static void store(
      final int value, final String className, final int lineNumber, final String variableName) {
    final var trace =
        new StoreTrace_int(
            traceCounter.getAndIncrement(), className, lineNumber, variableName, value);
    traceQueue.add(trace);
  }

  public static void store(
      final float value, final String className, final int lineNumber, final String variableName) {
    final var trace =
        new StoreTrace_float(
            traceCounter.getAndIncrement(), className, lineNumber, variableName, value);
    traceQueue.add(trace);
  }

  public static void store(
      final long value, final String className, final int lineNumber, final String variableName) {
    final var trace =
        new StoreTrace_long(
            traceCounter.getAndIncrement(), className, lineNumber, variableName, value);
    traceQueue.add(trace);
  }

  public static void store(
      final double value, final String className, final int lineNumber, final String variableName) {
    final var trace =
        new StoreTrace_double(
            traceCounter.getAndIncrement(), className, lineNumber, variableName, value);
    traceQueue.add(trace);
  }

  public static void store(
      final Object value, final String className, final int lineNumber, final String variableName) {
    final var trace =
        new StoreTrace_Object(
            traceCounter.getAndIncrement(), className, lineNumber, variableName, value);
    traceQueue.add(trace);
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

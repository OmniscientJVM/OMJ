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

@SuppressWarnings("unused")
public final class OMJAgentLib {

  private static final AtomicLong methodCounter = new AtomicLong(0);
  private static final ThreadLocal<MethodTrace> currentMethodTrace = new ThreadLocal<>();
  private static final ThreadLocal<Integer> currentLineNumber = ThreadLocal.withInitial(() -> 0);
  private static final ConcurrentLinkedQueue<MethodTrace> methodTraceQueue =
      new ConcurrentLinkedQueue<>();

  static {
    final Semaphore traceProcessorRunning = new Semaphore(1);

    final var traceProcessorThread =
        new Thread(
            () -> {
              traceProcessorRunning.acquireUninterruptibly();

              // TODO: This should probably use a memory-mapped file
              final var traceFile =
                  Util.cacheDir.resolve("trace_" + System.currentTimeMillis() + ".trace");
              try (final var os = new BufferedOutputStream(Files.newOutputStream(traceFile))) {
                loopWriteTraces(os);
                os.flush();
              } catch (IOException e) {
                e.printStackTrace();
                System.err.println(
                    "OMJ Agent-lib could not open the trace file " + traceFile.toString());

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
                  traceProcessorThread.interrupt();

                  // Wait for the trace processor to finish so data is flushed out
                  traceProcessorRunning.acquireUninterruptibly();
                }));

    traceProcessorThread.start();
  }

  private static void loopWriteTraces(final OutputStream os) {
    while (!Thread.currentThread().isInterrupted()) {
      final var methodTrace = methodTraceQueue.poll();
      if (methodTrace != null) {
        try {
          methodTrace.serialize(os);
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      // TODO: Don't busy-wait here
      try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    methodTraceQueue.forEach(
        methodTrace -> {
          try {
            methodTrace.serialize(os);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }

  public static void lineNumber(final int lineNumber) {
    currentLineNumber.set(lineNumber);
  }

  public static void methodCall_start(final MethodTrace methodTrace) {
    methodTrace.setIndex(methodCounter.getAndIncrement());
    methodTrace.setLineNumber(currentLineNumber.get());
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

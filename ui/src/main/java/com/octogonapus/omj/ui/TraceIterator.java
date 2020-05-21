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
package com.octogonapus.omj.ui;

import com.octogonapus.omj.util.SimpleTypeUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Iterates over a trace file to parse each trace in it. */
final class TraceIterator implements Iterator<Trace>, AutoCloseable {

  private final Logger logger = LoggerFactory.getLogger(TraceIterator.class);
  private final InputStream traceStream;

  TraceIterator(final InputStream traceStream) {
    this.traceStream = traceStream;
    // TODO: Validate the trace header once it is added in Issue #13
  }

  @Override
  public boolean hasNext() {
    try {
      traceStream.mark(1);
      final int readResult = traceStream.read();
      traceStream.reset();
      return readResult != -1;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public Trace next() {
    if (!hasNext()) {
      throw new NoSuchElementException("The trace stream is empty.");
    }

    try {
      return unsafeNext();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * The real implementation of {@link Iterator#next()}, put here to simplify exception handling
   * logic.
   *
   * @return The next Trace in the stream.
   * @throws IOException From reading from the trace stream.
   */
  private Trace unsafeNext() throws IOException {
    final long index =
        ByteBuffer.allocate(8)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put((byte) traceStream.read())
            .put((byte) traceStream.read())
            .put((byte) traceStream.read())
            .put((byte) traceStream.read())
            .put((byte) traceStream.read())
            .put((byte) traceStream.read())
            .put((byte) traceStream.read())
            .put((byte) traceStream.read())
            .rewind()
            .getLong();

    final byte type = (byte) Integer.parseUnsignedInt(Integer.toHexString(traceStream.read()), 16);
    if (type == 0x2) {
      return parseMethodTrace(index);
    } else {
      throw new UnsupportedOperationException("Unknown trace type: " + type);
    }
  }

  private Trace parseMethodTrace(final long index) throws IOException {
    // Parse location
    final var location = parseString();

    // Parse number of arguments
    final byte numArguments =
        (byte) Integer.parseUnsignedInt(Integer.toHexString(traceStream.read()), 16);

    // Parse arguments
    final var arguments = new ArrayList<MethodArgument>();
    for (int i = 0; i < numArguments; i++) {
      final byte typeByte = (byte) traceStream.read();
      final SimpleTypeUtil.SimpleType type =
          SimpleTypeUtil.getSimpleTypeFromDescriptorByte(typeByte);

      logger.debug("Parsed type {} from {}", type, Integer.toHexString(typeByte));

      if (type == SimpleTypeUtil.SimpleType.REFERENCE) {
        // Need to treat references specially here because SimpleTypeUtil.getLengthOfTypeForTrace
        // tries to compute the length based on the class name, which is not present until we parse
        // it right here.
        final String classType = parseString();

        final String hashCode =
            Integer.toHexString(traceStream.read())
                + Integer.toHexString(traceStream.read())
                + Integer.toHexString(traceStream.read())
                + Integer.toHexString(traceStream.read());

        arguments.add(new MethodArgument(classType, hashCode));
      } else {
        // Can use SimpleTypeUtil.getLengthOfTypeForTrace for the rest
        final int length = SimpleTypeUtil.getLengthOfTypeForTrace(type);
        final var valueBytes = traceStream.readNBytes(length);

        arguments.add(
            new MethodArgument(
                SimpleTypeUtil.getAdaptedClassName(type),
                parsePrimitiveBytesToString(type, valueBytes)));
      }
    }

    return new MethodTrace(index, location, arguments);
  }

  /**
   * Parses a string by reading from the {@link #traceStream} until a NULL byte is found. The NULL
   * byte is not appended to the returned string.
   *
   * @return The parsed string.
   * @throws IOException From reading from the {@link #traceStream}.
   */
  private String parseString() throws IOException {
    final var builder = new StringBuilder();

    while (true) {
      final int read = traceStream.read();

      // Look for null terminating character
      if (read == 0) {
        break;
      }

      builder.append((char) read);
    }

    return builder.toString();
  }

  /**
   * Parses the given bytes into a primitive of the given type. The bytes are expected to be in
   * little endian format.
   *
   * @param type The type to parse into.
   * @param bytes The bytes to parse.
   * @return A string containing the value of the primitive.
   */
  private String parsePrimitiveBytesToString(
      final SimpleTypeUtil.SimpleType type, final byte[] bytes) {
    // TODO: Waiting on a new google-java-format release to use enhanced switch statements
    switch (type) {
      case BOOLEAN:
        return bytes[0] == 0x1 ? "true" : "false";
      case CHAR:
        final char c =
            (char)
                (Byte.parseByte(Integer.toHexString(bytes[0]), 16)
                    | Byte.parseByte(Integer.toHexString(bytes[1]), 16) << 8);
        return "" + c;
      case BYTE:
        return "" + Integer.parseInt(Integer.toHexString(bytes[0]), 16);
      case SHORT:
        return ""
            + (Short.parseShort(Integer.toHexString(bytes[0]), 16)
                | Short.parseShort(Integer.toHexString(bytes[1]), 16) << 8);
      case INT:
        return ""
            + (Integer.parseInt(Integer.toHexString(bytes[0]), 16)
                | Integer.parseInt(Integer.toHexString(bytes[1]), 16) << 8
                | Integer.parseInt(Integer.toHexString(bytes[2]), 16) << 16
                | Integer.parseInt(Integer.toHexString(bytes[3]), 16) << 24);
      case FLOAT:
        return ""
            + ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(bytes[0])
                .put(bytes[1])
                .put(bytes[2])
                .put(bytes[3])
                .rewind()
                .getFloat();
      case LONG:
        return ""
            + ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(bytes[0])
                .put(bytes[1])
                .put(bytes[2])
                .put(bytes[3])
                .put(bytes[4])
                .put(bytes[5])
                .put(bytes[6])
                .put(bytes[7])
                .rewind()
                .getLong();
      case DOUBLE:
        return ""
            + ByteBuffer.allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(bytes[0])
                .put(bytes[1])
                .put(bytes[2])
                .put(bytes[3])
                .put(bytes[4])
                .put(bytes[5])
                .put(bytes[6])
                .put(bytes[7])
                .rewind()
                .getDouble();
      default:
        throw new IllegalArgumentException("Can't parse " + type + " into a primitive.");
    }
  }

  @Override
  public void close() throws Exception {
    traceStream.close();
  }
}

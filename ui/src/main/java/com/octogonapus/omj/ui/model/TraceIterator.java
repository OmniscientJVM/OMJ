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
package com.octogonapus.omj.ui.model;

import com.octogonapus.omj.util.SimpleTypeUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Iterates over a trace file to parse each trace in it. */
public final class TraceIterator implements Iterator<Trace>, AutoCloseable {

  private final Logger logger = LoggerFactory.getLogger(TraceIterator.class);
  private final InputStream traceStream;

  public TraceIterator(final InputStream traceStream) {
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

    final byte type = parseByte();

    switch (type) {
      case 0x1:
        return parseStoreTrace(index);
      case 0x2:
        return parseMethodTrace(index);
      case 0x3:
        return parseArrayStoreTrace(index);
      default:
        throw new UnsupportedOperationException("Unknown trace type: " + type);
    }
  }

  private byte parseByte() throws IOException {
    return (byte) Integer.parseUnsignedInt(Integer.toHexString(traceStream.read()), 16);
  }

  private boolean parseBoolean() throws IOException {
    return parseByte() != 0;
  }

  private Trace parseStoreTrace(final long index) throws IOException {
    // Parse class name
    final String className = parseString();
    logger.debug("className = {}", className);

    // Parse line number
    final int lineNumber = parseInt();
    logger.debug("lineNumber = {}", lineNumber);

    // Parse variable name
    final String variableName = parseString();
    logger.debug("variableName = {}", variableName);

    return new StoreTrace(index, className, lineNumber, variableName, parseTypeValuePair());
  }

  private Trace parseArrayStoreTrace(final long index) throws IOException {
    // Parse class name
    final String className = parseString();
    logger.debug("className = {}", className);

    // Parse line number
    final int lineNumber = parseInt();
    logger.debug("lineNumber = {}", lineNumber);

    // Parse array reference
    final String arrayRef = parseHashcode();

    // Parse array index
    final int arrayIndex = parseInt();

    // Parse value
    return new ArrayStoreTrace(
        index, className, lineNumber, arrayRef, arrayIndex, parseTypeValuePair());
  }

  private Trace parseMethodTrace(final long index) throws IOException {
    // Parse class name
    final String className = parseString();
    logger.debug("className = {}", className);

    // Parse line number
    final int lineNumber = parseInt();
    logger.debug("lineNumber = {}", lineNumber);

    // Parse method name
    final String methodName = parseString();
    logger.debug("methodName = {}", methodName);

    // Parse is static
    final boolean isStatic = parseBoolean();
    logger.debug("isStatic = {}", isStatic);

    // Parse number of arguments
    final byte numArguments = parseByte();
    logger.debug("numArguments = {}", numArguments);

    // Parse arguments
    final var arguments = new ArrayList<TypeValuePair>();
    for (int i = 0; i < numArguments; i++) {
      arguments.add(parseTypeValuePair());
    }

    return new MethodTrace(index, className, lineNumber, methodName, isStatic, arguments);
  }

  private TypeValuePair parseTypeValuePair() throws IOException {
    final SimpleTypeUtil.SimpleType type = parseType();

    if (type == SimpleTypeUtil.SimpleType.REFERENCE) {
      // Need to treat references specially here because SimpleTypeUtil.getLengthOfTypeForTrace
      // tries to compute the length based on the class name, which is not present until we parse
      // it right here.
      final String classType = parseString();

      if (classType.equals("java.lang.String")) {
        // For strings, the value is the length of the string and the bytes
        final int stringLength = parseInt();
        final String stringValue = parseString(stringLength);
        return new TypeValuePair(classType, stringValue);
      } else {
        // For objects, the value is the hashcode
        final String hashCode = parseHashcode();

        return new TypeValuePair(classType, hashCode);
      }
    } else {
      // Can use SimpleTypeUtil.getLengthOfTypeForTrace for the rest
      final int length = SimpleTypeUtil.getLengthOfTypeForTrace(type);
      final var valueBytes = traceStream.readNBytes(length);

      return new TypeValuePair(
          SimpleTypeUtil.getAdaptedClassName(type), parsePrimitiveBytesToString(type, valueBytes));
    }
  }

  @NotNull
  private String parseHashcode() throws IOException {
    return Integer.toHexString(traceStream.read())
        + Integer.toHexString(traceStream.read())
        + Integer.toHexString(traceStream.read())
        + Integer.toHexString(traceStream.read());
  }

  private SimpleTypeUtil.SimpleType parseType() throws IOException {
    final byte typeByte = (byte) traceStream.read();
    final SimpleTypeUtil.SimpleType type = SimpleTypeUtil.getSimpleTypeFromDescriptorByte(typeByte);

    logger.debug("Parsed type {} from {}", type, Integer.toHexString(typeByte));
    return type;
  }

  /**
   * Parses an int by reading four bytes in little-endian format.
   *
   * @return The parsed int.
   * @throws IOException From reading from the {@link #traceStream}.
   */
  private int parseInt() throws IOException {
    return Integer.parseInt(Integer.toHexString(traceStream.read()), 16)
        | Integer.parseInt(Integer.toHexString(traceStream.read()), 16) << 8
        | Integer.parseInt(Integer.toHexString(traceStream.read()), 16) << 16
        | Integer.parseInt(Integer.toHexString(traceStream.read()), 16) << 24;
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
   * Parses a string by reading from the {@link #traceStream} for a number of bytes.
   *
   * @param length The number of bytes in the string.
   * @return The parsed string.
   * @throws IOException From reading from the {@link #traceStream}.
   */
  private String parseString(final int length) throws IOException {
    final var builder = new StringBuilder();

    for (int i = 0; i < length; i++) {
      builder.append((char) traceStream.read());
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
    switch (type) {
      case BOOLEAN:
        return bytes[0] == 0x1 ? "true" : "false";
      case CHAR:
        return ""
            + ByteBuffer.allocate(2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(bytes[0])
                .put(bytes[1])
                .rewind()
                .getChar();
      case BYTE:
        return "" + Integer.parseInt(Integer.toHexString(bytes[0] & 0xFF), 16);
      case SHORT:
        return ""
            + ByteBuffer.allocate(2)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(bytes[0])
                .put(bytes[1])
                .rewind()
                .getShort();
      case INT:
        return ""
            + ByteBuffer.allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .put(bytes[0])
                .put(bytes[1])
                .put(bytes[2])
                .put(bytes[3])
                .rewind()
                .getInt();
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

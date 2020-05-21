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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class TraceIterator implements Iterator<Trace>, AutoCloseable {

  private final Logger logger = LoggerFactory.getLogger(TraceIterator.class);
  private final InputStream traceStream;

  public TraceIterator(final InputStream traceStream) {
    this.traceStream = traceStream;
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
    try {
      return unsafeNext();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  private Trace unsafeNext() throws IOException {
    final long index =
        Long.parseUnsignedLong(Long.toHexString(traceStream.read()), 16)
            | Long.parseUnsignedLong(Long.toHexString(traceStream.read()), 16) << 8
            | Long.parseUnsignedLong(Long.toHexString(traceStream.read()), 16) << 16
            | Long.parseUnsignedLong(Long.toHexString(traceStream.read()), 16) << 24
            | Long.parseUnsignedLong(Long.toHexString(traceStream.read()), 16) << 32
            | Long.parseUnsignedLong(Long.toHexString(traceStream.read()), 16) << 40
            | Long.parseUnsignedLong(Long.toHexString(traceStream.read()), 16) << 48
            | Long.parseUnsignedLong(Long.toHexString(traceStream.read()), 16) << 56;

    final int remainingLength =
        Integer.parseUnsignedInt(Integer.toHexString(traceStream.read()), 16)
            | Integer.parseUnsignedInt(Integer.toHexString(traceStream.read()), 16) << 8
            | Integer.parseUnsignedInt(Integer.toHexString(traceStream.read()), 16) << 16
            | Integer.parseUnsignedInt(Integer.toHexString(traceStream.read()), 16) << 24;
    logger.debug("remainingLength={}", remainingLength);

    final byte type = (byte) Integer.parseUnsignedInt(Integer.toHexString(traceStream.read()), 16);
    if (type == 0x2) {
      // Minus 1 because we read the type
      return parseMethodTrace(index, remainingLength - 1);
    } else {
      throw new UnsupportedOperationException("Unknown trace type: " + type);
    }
  }

  private Trace parseMethodTrace(final long index, final int remainingLength) throws IOException {
    int totalBytesRead = 0;

    // Parse location
    final var parsedLocation = parseString(totalBytesRead, remainingLength);
    totalBytesRead += parsedLocation.bytesRead;
    final var location = parsedLocation.string;

    // Parse arguments
    final var arguments = new ArrayList<MethodArgument>();
    while (totalBytesRead < remainingLength) {
      final byte typeByte = (byte) traceStream.read();
      final SimpleTypeUtil.SimpleType type = SimpleTypeUtil.getSimpleTypeFromDescriptorByte(typeByte);
      totalBytesRead++;

      logger.debug("Parsed type {} from {}", type, Integer.toHexString(typeByte));

      if (type == SimpleTypeUtil.SimpleType.REFERENCE) {
        // Need to treat references specially here because SimpleTypeUtil.getLengthOfTypeForTrace
        // tries to compute the length based on the class name, which is not present until we parse
        // it right here.
        final var parsedClassType = parseString(totalBytesRead, remainingLength);
        totalBytesRead += parsedClassType.bytesRead;
        final String classType = parsedClassType.string;

        final String hashCode =
            Integer.toHexString(traceStream.read()) + Integer.toHexString(traceStream.read()) +
            Integer.toHexString(traceStream.read()) + Integer.toHexString(traceStream.read());
        totalBytesRead += 4;

        arguments.add(new MethodArgument(classType, hashCode));
      } else {
        // Can use SimpleTypeUtil.getLengthOfTypeForTrace for the rest
        final int length = SimpleTypeUtil.getLengthOfTypeForTrace(type);
        final var valueBytes = traceStream.readNBytes(length);
        totalBytesRead += length;

        arguments.add(new MethodArgument(SimpleTypeUtil.getAdaptedClassName(type),
                                         parsePrimitiveBytesToString(type, valueBytes)));
      }
    }

    return new MethodTrace(index, location, arguments);
  }

  private ParsedString parseString(final int initialTotalBytesRead,
                                   final int remainingLength) throws IOException {
    int newTotalBytesRead = initialTotalBytesRead;
    final var builder = new StringBuilder();

    while (true) {
      if (newTotalBytesRead >= remainingLength) {
        throw new IllegalStateException("Unexpected end of stream.");
      }

      final int read = traceStream.read();
      newTotalBytesRead++;

      // Look for null terminating character
      if (read == 0) {
        break;
      }

      builder.append((char) read);
    }

    return new ParsedString(builder.toString(), newTotalBytesRead - initialTotalBytesRead);
  }

  private String parsePrimitiveBytesToString(final SimpleTypeUtil.SimpleType type,
                                             final byte[] bytes) {
    switch (type) {
      case BOOLEAN -> {
        return bytes[0] == 0x1 ? "true" : "false";
      }
      case CHAR -> {
        final char c = (char) ((char) bytes[0] |
                               ((char) (bytes[1] << 8)));
        return "" + c;
      }
      case BYTE -> {
        return "" + Integer.parseInt(Integer.toHexString(bytes[0]), 16);
      }
      case SHORT -> {
        return "" + (Short.parseShort(Integer.toHexString(bytes[0])) |
                     Short.parseShort(Integer.toHexString(bytes[1])) << 8);
      }
      case INT -> {
        return "" + (Integer.parseInt(Integer.toHexString(bytes[0])) |
                     Integer.parseInt(Integer.toHexString(bytes[1])) << 8 |
                     Integer.parseInt(Integer.toHexString(bytes[2])) << 16 |
                     Integer.parseInt(Integer.toHexString(bytes[3])) << 24);
      }
      case FLOAT -> {
        return "";
      }
      case LONG -> {
        return "" + (Long.parseUnsignedLong(Long.toHexString(bytes[0]), 16)
                     | Long.parseUnsignedLong(Long.toHexString(bytes[1]), 16) << 8
                     | Long.parseUnsignedLong(Long.toHexString(bytes[2]), 16) << 16
                     | Long.parseUnsignedLong(Long.toHexString(bytes[3]), 16) << 24
                     | Long.parseUnsignedLong(Long.toHexString(bytes[4]), 16) << 32
                     | Long.parseUnsignedLong(Long.toHexString(bytes[5]), 16) << 40
                     | Long.parseUnsignedLong(Long.toHexString(bytes[6]), 16) << 48
                     | Long.parseUnsignedLong(Long.toHexString(bytes[7]), 16) << 56);
      }
      case DOUBLE -> {
        return "";
      }
      default -> throw new IllegalArgumentException("Can't parse " + type + " into a primitive.");
    }
  }

  @Override
  public void close() throws Exception {
    traceStream.close();
  }

  private static class ParsedString {
    String string;
    int bytesRead;

    public ParsedString(final String string, final int bytesRead) {
      this.string = string;
      this.bytesRead = bytesRead;
    }
  }
}

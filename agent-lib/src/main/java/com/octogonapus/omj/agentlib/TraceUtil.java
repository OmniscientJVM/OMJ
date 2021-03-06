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

import java.io.IOException;
import java.io.OutputStream;

public class TraceUtil {

  static void writeStoreTraceHeader(
      final OutputStream outputStream,
      final String className,
      final long index,
      final int lineNumber,
      final String variableName)
      throws IOException {
    write8Bytes(outputStream, index);

    outputStream.write(0x1);

    writeNullTerminatedString(outputStream, className);
    write4Bytes(outputStream, lineNumber);
    writeNullTerminatedString(outputStream, variableName);
  }

  static void writeArrayStoreTraceHeader(
      final OutputStream outputStream,
      final String className,
      final long index,
      final int lineNumber,
      final Object array,
      final int arrayIndex)
      throws IOException {
    write8Bytes(outputStream, index);

    outputStream.write(0x3);

    writeNullTerminatedString(outputStream, className);
    write4Bytes(outputStream, lineNumber);
    write4Bytes(outputStream, System.identityHashCode(array));
    write4Bytes(outputStream, arrayIndex);
  }

  static void write4Bytes(final OutputStream outputStream, final int lineNumber)
      throws IOException {
    outputStream.write((byte) (lineNumber & 0xFF));
    outputStream.write((byte) ((lineNumber >> 8) & 0xFF));
    outputStream.write((byte) ((lineNumber >> 16) & 0xFF));
    outputStream.write((byte) ((lineNumber >> 24) & 0xFF));
  }

  static void write8Bytes(final OutputStream outputStream, final long index) throws IOException {
    outputStream.write((byte) (index & 0xFF));
    outputStream.write((byte) ((index >> 8) & 0xFF));
    outputStream.write((byte) ((index >> 16) & 0xFF));
    outputStream.write((byte) ((index >> 24) & 0xFF));
    outputStream.write((byte) ((index >> 32) & 0xFF));
    outputStream.write((byte) ((index >> 40) & 0xFF));
    outputStream.write((byte) ((index >> 48) & 0xFF));
    outputStream.write((byte) ((index >> 56) & 0xFF));
  }

  static void writeNullTerminatedString(final OutputStream outputStream, final String string)
      throws IOException {
    outputStream.write(string.getBytes());
    outputStream.write(0);
  }

  static void writeObject(final OutputStream outputStream, final Object value) throws IOException {
    writeNullTerminatedString(outputStream, value.getClass().getName());
    if (value instanceof String) {
      final byte[] value_string_bytes = ((String) value).getBytes();
      write4Bytes(outputStream, value_string_bytes.length);
      outputStream.write(value_string_bytes);
    } else {
      write4Bytes(outputStream, System.identityHashCode(value));
    }
  }
}

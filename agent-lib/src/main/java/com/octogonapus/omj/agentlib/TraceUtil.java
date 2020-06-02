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
      final int lineNumber)
      throws IOException {
    write8Bytes(outputStream, index);

    outputStream.write(0x1);

    outputStream.write(className.getBytes());
    outputStream.write(0);

    write4Bytes(outputStream, lineNumber);
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
}

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

public class StoreTrace_array_byte implements Trace {

  private final long index;
  private final byte[] array;
  private final int arrayIndex;
  private final byte value;
  private final String className;
  private final int lineNumber;
  private final String variableName;

  public StoreTrace_array_byte(
      final long index,
      final byte[] array,
      final int arrayIndex,
      final byte value,
      final String className,
      final int lineNumber,
      final String variableName) {
    this.index = index;
    this.array = array;
    this.arrayIndex = arrayIndex;
    this.value = value;
    this.className = className;
    this.lineNumber = lineNumber;
    this.variableName = variableName;
  }

  @Override
  public void serialize(final OutputStream outputStream) throws IOException {
    TraceUtil.writeArrayStoreTraceHeader(
        outputStream, className, index, lineNumber, variableName, array, arrayIndex);
    outputStream.write('B');
    outputStream.write(value);
  }
}

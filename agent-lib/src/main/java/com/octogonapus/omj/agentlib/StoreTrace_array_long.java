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

public class StoreTrace_array_long implements Trace {

  private final long index;
  private final long[] array;
  private final int arrayIndex;
  private final long value;
  private final String className;
  private final int lineNumber;

  public StoreTrace_array_long(
      final long index,
      final long[] array,
      final int arrayIndex,
      final long value,
      final String className,
      final int lineNumber) {
    this.index = index;
    this.array = array;
    this.arrayIndex = arrayIndex;
    this.value = value;
    this.className = className;
    this.lineNumber = lineNumber;
  }

  @Override
  public long getIndex() {
    return index;
  }

  @Override
  public void serialize(final OutputStream outputStream) throws IOException {
    TraceUtil.writeArrayStoreTraceHeader(
        outputStream, className, index, lineNumber, array, arrayIndex);
    outputStream.write('J');
    TraceUtil.write8Bytes(outputStream, value);
  }
}

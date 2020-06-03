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

public class StoreTrace_double implements Trace {

  private final long index;
  private final String className;
  private final int lineNumber;
  private final String variableName;
  private final double value;

  public StoreTrace_double(
      final long index,
      final String className,
      final int lineNumber,
      final String variableName,
      final double value) {
    this.index = index;
    this.className = className;
    this.lineNumber = lineNumber;
    this.variableName = variableName;
    this.value = value;
  }

  @Override
  public void serialize(final OutputStream outputStream) throws IOException {
    TraceUtil.writeStoreTraceHeader(outputStream, className, index, lineNumber, variableName);
    outputStream.write('D');
    TraceUtil.write8Bytes(outputStream, Double.doubleToRawLongBits(value));
  }
}

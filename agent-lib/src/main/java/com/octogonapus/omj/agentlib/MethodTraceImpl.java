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

import java.io.BufferedOutputStream;
import java.io.IOException;

public class MethodTraceImpl implements MethodTrace {

  private String methodLocation;
  private int Object_counter = 0;
  private Object Object_0;

  public MethodTraceImpl(final String methodLocation) {
    this.methodLocation = methodLocation;
  }

  @Override
  public void set_argument_Object(final Object value) {
    switch (Object_counter) {
      case 0:
        Object_0 = value;
    }
    Object_counter++;
  }

  @Override
  public void serialize(final BufferedOutputStream outputStream) throws IOException {
    // { M Object <Object ref>
    outputStream.write('{');
    outputStream.write('M');
    outputStream.write('L');
    outputStream.write(System.identityHashCode(Object_0));
  }
}

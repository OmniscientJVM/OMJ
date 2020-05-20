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

@SuppressWarnings("unused")
public abstract class MethodTrace {

  protected long index;
  protected final String methodLocation;

  public MethodTrace(final String methodLocation) {
    this.methodLocation = methodLocation;
  }

  public void setIndex(final long index) {
    this.index = index;
  }

  public void set_argument_boolean(final boolean value) {}

  public void set_argument_char(final char value) {}

  public void set_argument_byte(final byte value) {}

  public void set_argument_short(final short value) {}

  public void set_argument_int(final int value) {}

  public void set_argument_float(final float value) {}

  public void set_argument_long(final long value) {}

  public void set_argument_double(final double value) {}

  public void set_argument_Object(final Object value) {}

  public abstract void serialize(final OutputStream outputStream) throws IOException;
}

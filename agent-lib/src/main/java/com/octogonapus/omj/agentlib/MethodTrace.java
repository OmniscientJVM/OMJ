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

@SuppressWarnings("unused")
public abstract class MethodTrace implements Trace {

  protected final boolean isStatic;
  protected long index;
  protected String className;
  protected int lineNumber;
  protected String methodName;

  public MethodTrace(final boolean isStatic) {
    this.isStatic = isStatic;
  }

  public void setIndex(final long index) {
    this.index = index;
  }

  @Override
  public long getIndex() {
    return index;
  }

  public void setClassName(final String className) {
    this.className = className;
  }

  public void setLineNumber(final int lineNumber) {
    this.lineNumber = lineNumber;
  }

  public void setMethodName(final String methodName) {
    this.methodName = methodName;
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
}

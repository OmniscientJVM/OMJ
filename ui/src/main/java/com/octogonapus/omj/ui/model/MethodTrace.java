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

import java.util.List;
import java.util.Objects;

public class MethodTrace implements Trace {

  public long index;
  public String location;
  public List<MethodArgument> arguments;

  public MethodTrace(
      final long index, final String location, final List<MethodArgument> arguments) {
    this.index = index;
    this.location = location;
    this.arguments = arguments;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final MethodTrace that = (MethodTrace) o;
    return index == that.index
        && Objects.equals(location, that.location)
        && Objects.equals(arguments, that.arguments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(index, location, arguments);
  }

  @Override
  public String toString() {
    return "MethodTrace{"
        + "index="
        + index
        + ", location='"
        + location
        + '\''
        + ", arguments="
        + arguments
        + '}';
  }
}

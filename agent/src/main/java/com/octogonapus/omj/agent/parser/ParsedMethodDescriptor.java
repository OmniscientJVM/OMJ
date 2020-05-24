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
package com.octogonapus.omj.agent.parser;

import java.util.List;
import java.util.Objects;

public final class ParsedMethodDescriptor {

  public List<Character> argumentTypes;
  public Character returnType;

  ParsedMethodDescriptor(final List<Character> argumentTypes, final Character returnType) {
    this.argumentTypes = argumentTypes;
    this.returnType = returnType;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final ParsedMethodDescriptor that = (ParsedMethodDescriptor) o;
    return Objects.equals(argumentTypes, that.argumentTypes)
        && Objects.equals(returnType, that.returnType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(argumentTypes, returnType);
  }

  @Override
  public String toString() {
    return "ParsedDescriptor{"
        + "argumentTypes="
        + argumentTypes
        + ", returnType="
        + returnType
        + '}';
  }
}

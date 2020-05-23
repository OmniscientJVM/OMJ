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
package com.agenttest.objectTestDataClass;

public class TestDataClass {

  private final int int_1;
  private final String string_1;

  public TestDataClass(int int_1, String string_1) {
    this.int_1 = int_1;
    this.string_1 = string_1;
  }

  @Override
  public String toString() {
    return "TestDataClass{" + "int_1=" + int_1 + ", string_1='" + string_1 + '\'' + '}';
  }
}

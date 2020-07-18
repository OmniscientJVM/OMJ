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
package com.agenttest.invokeDynamicWithBooleanTrue;

import java.util.function.Function;

public class Main {

  public static void main(String[] args) {
    ((Function<Integer, String>)
            (Integer i) -> {
              return foo(i);
            })
        .apply(1);
  }

  private static String foo(Integer i) {
    return "" + i;
  }
}

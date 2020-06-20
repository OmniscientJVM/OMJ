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
package com.agenttest.storeIntMultithreaded;

import java.util.concurrent.Semaphore;

public class Main {

  static volatile int i;

  public static void main(String[] args) {
    Semaphore s = new Semaphore(1);

    var t1 =
        new Thread(
            () -> {
              for (int j = 0; j < 20; j++) {
                if (j % 2 == 0) {
                  s.acquireUninterruptibly();
                  i = j;
                  s.release();
                }
              }
            });

    var t2 =
        new Thread(
            () -> {
              for (int j = 0; j < 20; j++) {
                if (j % 2 != 0) {
                  s.acquireUninterruptibly();
                  i = j;
                  s.release();
                }
              }
            });

    t1.start();
    t2.start();

    try {
      t1.join();
    } catch (InterruptedException e) {
      System.exit(1);
    }

    try {
      t2.join();
    } catch (InterruptedException e) {
      System.exit(1);
    }
  }
}

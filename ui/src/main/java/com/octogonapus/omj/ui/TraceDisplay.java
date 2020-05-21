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
package com.octogonapus.omj.ui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import javafx.scene.layout.AnchorPane;

public class TraceDisplay extends AnchorPane {

  public TraceDisplay(final File traceFile) {
    new Thread(() -> loadTrace(traceFile)).start();
  }

  private void loadTrace(final File traceFile) {
    try (final var iter =
        new TraceIterator(new BufferedInputStream(new FileInputStream(traceFile)))) {
      while (iter.hasNext()) {
        final Trace trace = iter.next();
        System.out.println("trace = " + trace);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

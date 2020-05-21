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

import javafx.scene.control.ListCell;

final class TraceCell extends ListCell<Trace> {

  @Override
  protected void updateItem(final Trace item, final boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setText(null);
      setGraphic(null);
    } else {
      if (item instanceof MethodTrace) {
        final MethodTrace methodTrace = (MethodTrace) item;

        final StringBuilder builder =
            new StringBuilder()
                .append(methodTrace.index)
                .append(' ')
                .append(methodTrace.location)
                .append('(');

        for (final var iter = methodTrace.arguments.iterator(); iter.hasNext(); ) {
          final MethodArgument arg = iter.next();
          builder.append(arg.getType()).append(": ").append(arg.getValue());
          if (iter.hasNext()) {
            builder.append(", ");
          }
        }

        builder.append(')');

        setText(builder.toString());
      }
    }
  }
}

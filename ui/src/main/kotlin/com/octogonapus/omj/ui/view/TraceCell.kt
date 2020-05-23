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
package com.octogonapus.omj.ui.view

import com.octogonapus.omj.ui.model.MethodTrace
import com.octogonapus.omj.ui.model.Trace
import javafx.scene.control.ListCell

class TraceCell : ListCell<Trace?>() {

    override fun updateItem(item: Trace?, empty: Boolean) {
        super.updateItem(item, empty)

        if (empty || item == null) {
            text = null
            graphic = null
        } else {
            if (item is MethodTrace) {
                val (index, callerClass, callerLine, methodName, isStatic, arguments) = item

                val argumentString = arguments.joinToString(separator = ", ") {
                    "${it.type}: ${it.value}"
                }

                val staticString = if (isStatic) " <static> " else " "

                text = "$index $callerClass:$callerLine$staticString$methodName($argumentString)"
            }
        }
    }
}

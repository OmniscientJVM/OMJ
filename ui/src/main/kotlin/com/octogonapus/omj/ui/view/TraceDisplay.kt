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

import com.octogonapus.omj.ui.model.Trace
import com.octogonapus.omj.ui.model.TraceIterator
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import javafx.scene.control.ListView
import kotlin.concurrent.thread

internal class TraceDisplay(traceFile: File) : ListView<Trace?>() {

    init {
        setCellFactory { TraceCell() }

        thread(start = true) {
            loadTrace(traceFile)
        }
    }

    private fun loadTrace(traceFile: File) {
        TraceIterator(BufferedInputStream(FileInputStream(traceFile))).use {
            items.addAll(it.asSequence())
        }
    }
}

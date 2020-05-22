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
package com.octogonapus.omj.ui.model

import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.nio.file.Paths
import org.junit.jupiter.api.Test

internal class TraceIteratorTest {

    @Test
    fun parseNoArgMethodCall() {
        val traces = loadTraces("noarg_method_call.trace")

        traceIndexShouldMatchListIndex(traces)

        traces.shouldHaveSingleElement {
            it is MethodTrace &&
                    it.arguments.size == 1 &&
                    it.arguments[0].type.contains("com.octogonapus.PrintHello")
        }
    }

    companion object {

        private fun traceIndexShouldMatchListIndex(traces: List<Trace>) {
            traces.forEachIndexed { index, trace ->
                trace.shouldBeInstanceOf<MethodTrace> {
                    it.index.shouldBe(index)
                }
            }
        }

        private fun loadTraces(name: String): List<Trace> = getIter(name).asSequence().toList()

        private fun getIter(name: String) =
                TraceIterator(BufferedInputStream(FileInputStream(
                        Paths.get(TraceIteratorTest::class.java.getResource(name).toURI()).toFile()
                )))
    }
}

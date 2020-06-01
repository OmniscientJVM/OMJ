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
package com.octogonapus.omj.testutil

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.neverNullMatcher
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot

fun <T> Array<T>.shouldHaveInOrder(vararg ps: (T) -> Boolean) =
    asList().shouldHaveInOrder(ps.toList())

fun <T> List<T>.shouldHaveInOrder(vararg ps: (T) -> Boolean) =
    this.shouldHaveInOrder(ps.toList())

infix fun <T> Array<T>.shouldHaveInOrder(expected: List<(T) -> Boolean>) =
    asList().shouldHaveInOrder(expected)

infix fun <T> List<T>.shouldHaveInOrder(expected: List<(T) -> Boolean>) =
    this should hasInOrder(expected)

infix fun <T> Array<T>.shouldNotHaveInOrder(expected: Array<(T) -> Boolean>) =
    asList().shouldNotHaveInOrder(expected.asList())

infix fun <T> Array<T>.shouldNotHaveInOrder(expected: List<(T) -> Boolean>) =
    asList().shouldNotHaveInOrder(expected)

infix fun <T> List<T>.shouldNotHaveInOrder(expected: List<(T) -> Boolean>) =
    this shouldNot hasInOrder(expected)

fun <T> hasInOrder(vararg ps: (T) -> Boolean): Matcher<Collection<T>?> = hasInOrder(ps.asList())

/**
 * Assert that a collection has a subsequence matching the sequence of predicates, possibly with
 * values in between.
 */
fun <T> hasInOrder(predicates: List<(T) -> Boolean>): Matcher<Collection<T>?> =
    neverNullMatcher { actual ->
        require(predicates.isNotEmpty()) { "predicates must not be empty" }

        var subsequenceIndex = 0
        val actualIterator = actual.iterator()

        while (actualIterator.hasNext() && subsequenceIndex < predicates.size) {
            if (predicates[subsequenceIndex](actualIterator.next())) subsequenceIndex += 1
        }

        MatcherResult(
            subsequenceIndex == predicates.size,
            { "$actual did not match the predicates $predicates in order" },
            { "$actual should not match the predicates $predicates in order" }
        )
    }
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

import org.junit.jupiter.api.AfterEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.ModuleDeclaration
import org.koin.dsl.module
import org.koin.test.KoinTest

open class KoinTestFixture : KoinTest {

    private var additionalAfterEach: () -> Unit = {}

    @AfterEach
    fun afterEach() {
        additionalAfterEach()
        stopKoin()
    }

    fun additionalAfterEach(configure: () -> Unit) {
        additionalAfterEach = configure
    }

    companion object {

        fun testKoin(moduleDeclaration: ModuleDeclaration) = startKoin {
            modules(module(moduleDeclaration = moduleDeclaration))
        }
    }
}

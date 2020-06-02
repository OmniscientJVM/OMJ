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
package com.octogonapus.omj.agent

import com.octogonapus.omj.testutil.KoinTestFixture
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.koin.dsl.module

internal class OMJClassFileTransformerTest : KoinTestFixture() {

    companion object {
        private const val className = "ClassName"
    }

    @Test
    fun `visit class that passes the filter`() {
        val inputByteArray = byteArrayOf(1, 2, 3)
        val expectedByteArray = byteArrayOf(4, 5, 6)
        val transformer = mockk<OMJClassFileTransformer.Transformer> {
            every { transformClassBytes(inputByteArray) } returns expectedByteArray
        }

        testKoin(
            module {
                single {
                    mockk<ClassFilter> {
                        every { shouldTransform(className) } returns true
                    }
                }
            }
        )

        val classFileTransformer = OMJClassFileTransformer(transformer)
        classFileTransformer.transform(
            null,
            className,
            null,
            mockk(),
            inputByteArray
        ).shouldBe(expectedByteArray)
    }

    @Test
    fun `visit class that does not pass the filter`() {
        testKoin(
            module {
                single {
                    mockk<ClassFilter> {
                        every { shouldTransform(className) } returns false
                    }
                }
            }
        )

        val classFileTransformer = OMJClassFileTransformer()

        // Returning null obeys the ClassFileTransformer contract. We can't just return the input
        // byte array.
        classFileTransformer.transform(
            null,
            className,
            null,
            mockk(),
            byteArrayOf(1, 2, 3)
        ).shouldBeNull()
    }
}

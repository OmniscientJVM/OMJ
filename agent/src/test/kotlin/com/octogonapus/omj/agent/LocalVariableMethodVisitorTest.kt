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

import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes.ASM8

internal class LocalVariableMethodVisitorTest {

    @Test
    fun `visit int`() {
        val visitor = LocalVariableMethodVisitor(ASM8, null)

        visitor.visitLocalVariable(
            "i",
            "I",
            null,
            mockk(),
            mockk(),
            1
        )

        visitor.locals.shouldContainExactly(LocalVariable("i", "I", 1))
    }

    @Test
    fun `visit double`() {
        val visitor = LocalVariableMethodVisitor(ASM8, null)

        visitor.visitLocalVariable(
            "d",
            "D",
            null,
            mockk(),
            mockk(),
            1
        )

        visitor.locals.shouldContainExactly(LocalVariable("d", "D", 1))
    }

    @Test
    fun `visit int after double`() {
        val visitor = LocalVariableMethodVisitor(ASM8, null)

        visitor.visitLocalVariable(
            "d",
            "D",
            null,
            mockk(),
            mockk(),
            1
        )

        visitor.visitLocalVariable(
            "i",
            "I",
            null,
            mockk(),
            mockk(),
            3 // 3 instead of 2 because doubles take up two slots
        )

        visitor.locals.shouldContainExactly(LocalVariable("d", "D", 1), LocalVariable("i", "I", 3))
    }
}

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
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes.ASM8

internal class LocalVariableClassVisitorTest {

    @Test
    fun `visit two methods`() {
        val visitor = LocalVariableClassVisitor(ASM8, null)
        visitor.visitMethod(
            0,
            "method1",
            "(I)V",
            null,
            null
        )
        visitor.visitMethod(
            0,
            "method2",
            "(DLjava/lang/String;)Z",
            null,
            null
        )

        val method1 = Method("method1", "(I)V")
        val method2 = Method("method2", "(DLjava/lang/String;)Z")
        visitor.localVariableVisitors.keys.shouldContainExactly(
            method1,
            method2
        )
    }
}

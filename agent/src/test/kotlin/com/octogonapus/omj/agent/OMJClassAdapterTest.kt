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

import com.octogonapus.omj.util.Util
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ASM8

internal class OMJClassAdapterTest {

    companion object {
        private const val className = "ClassName"
        private const val superName = "SuperName"
        private const val methodName = "methodName"
        private const val methodDescriptor = "(I)V"
    }

    @Test
    fun `visit normal method`() {
        val classAdapter = getClassAdapter()

        classAdapter.visitMethod(
            0,
            methodName,
            methodDescriptor,
            null,
            null
        ).shouldBeInstanceOf<OMJMethodAdapter>()
    }

    @Test
    fun `visit instance initialization method`() {
        val classAdapter = getClassAdapter()

        classAdapter.visitMethod(
            0,
            "<init>",
            "()V",
            null,
            null
        ).shouldBeInstanceOf<OMJInstanceInitializationMethodAdapter>()
    }

    @Test
    fun `visit class instance initialization method`() {
        val classAdapter = getClassAdapter()

        classAdapter.visitMethod(
            0,
            "<clinit>",
            "()V",
            null,
            null
        ).shouldBeInstanceOf<OMJMethodAdapter>()
    }

    @Test
    fun `visit main method`() {
        val classAdapter = getClassAdapter()

        classAdapter.visitMethod(
            ACC_PUBLIC + ACC_STATIC,
            "main",
            Util.mainMethodDescriptor,
            null,
            null
        ).shouldBeInstanceOf<OMJMainMethodAdapter>()
    }

    @Test
    fun `visit method named main`() {
        val classAdapter = getClassAdapter()

        // The real main also needs to be public and static
        classAdapter.visitMethod(
            0,
            "main",
            Util.mainMethodDescriptor,
            null,
            null
        ).shouldBeInstanceOf<OMJMethodAdapter>()
    }

    @Test
    fun `visit normal method in Object class`() {
        val expectedMethodVisitor = mockk<MethodVisitor>("ExpectedMethodVisitor")
        val classAdapter = OMJClassAdapter(
            ASM8,
            mockk(relaxed = true) {
                every {
                    visitMethod(0, methodName, methodDescriptor, null, null)
                } returns expectedMethodVisitor
            }
        )
        classAdapter.visit(
            ASM8,
            0,
            "Object",
            null,
            null,
            null
        )

        classAdapter.visitMethod(
            0,
            methodName,
            methodDescriptor,
            null,
            null
        ).shouldBe(expectedMethodVisitor)
    }

    private fun getClassAdapter(): OMJClassAdapter {
        val classAdapter = OMJClassAdapter(ASM8, mockk(relaxed = true))
        classAdapter.visit(
            ASM8,
            0,
            className,
            null,
            superName,
            null
        )
        return classAdapter
    }
}

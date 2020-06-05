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

import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList

internal sealed class InsnListInsertion {

    /**
     * Perform the insertion and modify the [InsnList].
     */
    abstract fun insert()

    /**
     * Places [toInsert] immediately before [nextInsn] so the list becomes:
     * [toInsert] -> [nextInsn].
     */
    internal data class InsertBefore(
        val insnList: InsnList,
        val nextInsn: AbstractInsnNode,
        val toInsert: InsnList
    ) : InsnListInsertion() {

        override fun insert() {
            insnList.insertBefore(nextInsn, toInsert)
        }
    }

    /**
     * Places [toInsert] immediately following [previousInsn] so the list becomes:
     * [previousInsn] -> [toInsert].
     */
    internal data class InsertAfter(
        val insnList: InsnList,
        val previousInsn: AbstractInsnNode,
        val toInsert: InsnList
    ) : InsnListInsertion() {

        override fun insert() {
            insnList.insert(previousInsn, toInsert)
        }
    }
}

internal inline fun InsnList.insertBefore(
    insnNode: AbstractInsnNode,
    toInsert: InsnList.() -> Unit
) = InsnListInsertion.InsertBefore(
    this,
    insnNode,
    InsnList().apply(toInsert)
)

internal inline fun InsnList.insertAfter(
    insnNode: AbstractInsnNode,
    toInsert: InsnList.() -> Unit
) = InsnListInsertion.InsertAfter(
    this,
    insnNode,
    InsnList().apply(toInsert)
)

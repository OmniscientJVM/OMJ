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

import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.DUP2
import org.objectweb.asm.Opcodes.DUP2_X1
import org.objectweb.asm.Opcodes.DUP_X1
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.Opcodes.PUTFIELD
import org.objectweb.asm.Opcodes.PUTSTATIC

object OpcodeUtil {

    /**
     * Picks a [DUP] or [DUP2] opcode for a *STORE opcode.
     *
     * @return The corresponding [DUP] or [DUP2] opcode based on the store [opcode].
     */
    fun getDupOpcode(opcode: Int): Int {
        return when (opcode) {
            ISTORE, FSTORE, ASTORE -> DUP
            LSTORE, DSTORE -> DUP2
            else -> throw UnsupportedOperationException(
                "Cannot get the DUP opcode for a non-store opcode: $opcode"
            )
        }
    }

    /**
     * Picks a dup opcode for a [PUTFIELD] or [PUTSTATIC] opcode.
     *
     * @return The corresponding [DUP] or [DUP2] opcode based on the store [opcode] and
     * [fieldDescriptor].
     */
    fun getDupOpcode(opcode: Int, fieldDescriptor: String): Int {
        check(opcode == PUTFIELD || opcode == PUTSTATIC)

        // Simplify the descriptor because references and arrays are just size L
        val adaptedDescriptor = when {
            fieldDescriptor.startsWith('L') || fieldDescriptor.startsWith('[') -> "L"
            else -> fieldDescriptor
        }

        return when (adaptedDescriptor) {
            "B", "C", "I", "F", "L", "S", "Z" -> when (opcode) {
                PUTFIELD -> DUP_X1
                PUTSTATIC -> DUP
                else -> error("Illegal opcode: $opcode")
            }

            "J", "D" -> when (opcode) {
                PUTFIELD -> DUP2_X1
                PUTSTATIC -> DUP2
                else -> error("Illegal opcode: $opcode")
            }

            else -> throw UnsupportedOperationException("Unknown field descriptor $fieldDescriptor")
        }
    }

    /**
     * @return The corresponding descriptor based on the store [opcode].
     */
    fun getStoreDescriptor(opcode: Int) = when (opcode) {
        ISTORE -> "I"
        LSTORE -> "J"
        FSTORE -> "F"
        DSTORE -> "D"
        ASTORE -> "Ljava/lang/Object;"
        else -> throw UnsupportedOperationException(
            "Cannot get the store descriptor for a non-store opcode: $opcode"
        )
    }
}

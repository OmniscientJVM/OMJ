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
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LSTORE

object OpcodeUtil {

    /**
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

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
package com.octogonapus.omj.agent.interpreter

import org.objectweb.asm.Opcodes.AALOAD
import org.objectweb.asm.Opcodes.AASTORE
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.BALOAD
import org.objectweb.asm.Opcodes.BASTORE
import org.objectweb.asm.Opcodes.CALOAD
import org.objectweb.asm.Opcodes.CASTORE
import org.objectweb.asm.Opcodes.DALOAD
import org.objectweb.asm.Opcodes.DASTORE
import org.objectweb.asm.Opcodes.DLOAD
import org.objectweb.asm.Opcodes.DSTORE
import org.objectweb.asm.Opcodes.FALOAD
import org.objectweb.asm.Opcodes.FASTORE
import org.objectweb.asm.Opcodes.FLOAD
import org.objectweb.asm.Opcodes.FSTORE
import org.objectweb.asm.Opcodes.IALOAD
import org.objectweb.asm.Opcodes.IASTORE
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.ISTORE
import org.objectweb.asm.Opcodes.LALOAD
import org.objectweb.asm.Opcodes.LASTORE
import org.objectweb.asm.Opcodes.LLOAD
import org.objectweb.asm.Opcodes.LSTORE
import org.objectweb.asm.Opcodes.SALOAD
import org.objectweb.asm.Opcodes.SASTORE
import org.objectweb.asm.Opcodes.T_BYTE
import org.objectweb.asm.Opcodes.T_CHAR
import org.objectweb.asm.Opcodes.T_DOUBLE
import org.objectweb.asm.Opcodes.T_FLOAT
import org.objectweb.asm.Opcodes.T_INT
import org.objectweb.asm.Opcodes.T_LONG
import org.objectweb.asm.Opcodes.T_SHORT

internal object OperandUtil {

    internal fun operandForArrayInsn(opcode: Int): Operand.RefType = when (opcode) {
        IALOAD, IASTORE -> Operand.RefType.ArrayRef(T_INT)
        LALOAD, LASTORE -> Operand.RefType.ArrayRef(T_LONG)
        FALOAD, FASTORE -> Operand.RefType.ArrayRef(T_FLOAT)
        DALOAD, DASTORE -> Operand.RefType.ArrayRef(T_DOUBLE)
        AALOAD, AASTORE -> Operand.RefType.RefArrayRef
        BALOAD, BASTORE -> Operand.RefType.ArrayRef(T_BYTE)
        CALOAD, CASTORE -> Operand.RefType.ArrayRef(T_CHAR)
        SALOAD, SASTORE -> Operand.RefType.ArrayRef(T_SHORT)
        else -> throw IllegalArgumentException("Cannot get the operand for opcode $opcode")
    }

    internal fun operandForVarInsn(opcode: Int): Operand = when (opcode) {
        ILOAD, ISTORE -> Operand.IntType.RuntimeInt
        LLOAD, LSTORE -> Operand.LongType.RuntimeLong
        FLOAD, FSTORE -> Operand.FloatType.RuntimeFloat
        DLOAD, DSTORE -> Operand.DoubleType.RuntimeDouble
        ALOAD, ASTORE -> Operand.RefType.RuntimeRef
        else -> throw IllegalArgumentException("Cannot get the operand for opcode $opcode")
    }
}

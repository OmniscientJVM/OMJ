package com.octogonapus.omj.agent.interpreter

import org.objectweb.asm.Opcodes.AALOAD
import org.objectweb.asm.Opcodes.AASTORE
import org.objectweb.asm.Opcodes.ACONST_NULL
import org.objectweb.asm.Opcodes.BALOAD
import org.objectweb.asm.Opcodes.BASTORE
import org.objectweb.asm.Opcodes.BIPUSH
import org.objectweb.asm.Opcodes.CALOAD
import org.objectweb.asm.Opcodes.CASTORE
import org.objectweb.asm.Opcodes.DALOAD
import org.objectweb.asm.Opcodes.DASTORE
import org.objectweb.asm.Opcodes.DCONST_0
import org.objectweb.asm.Opcodes.FALOAD
import org.objectweb.asm.Opcodes.FASTORE
import org.objectweb.asm.Opcodes.FCONST_0
import org.objectweb.asm.Opcodes.IALOAD
import org.objectweb.asm.Opcodes.IASTORE
import org.objectweb.asm.Opcodes.ICONST_M1
import org.objectweb.asm.Opcodes.LALOAD
import org.objectweb.asm.Opcodes.LASTORE
import org.objectweb.asm.Opcodes.LCONST_0
import org.objectweb.asm.Opcodes.NEWARRAY
import org.objectweb.asm.Opcodes.NOP
import org.objectweb.asm.Opcodes.SALOAD
import org.objectweb.asm.Opcodes.SASTORE
import org.objectweb.asm.Opcodes.SIPUSH
import org.objectweb.asm.Opcodes.T_BOOLEAN
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
}

package com.octogonapus.omj.agent.interpreter

internal sealed class Operand {

    sealed class IntType : Operand() {
        data class ConstInt(val value: Int) : IntType()
        object RuntimeInt : IntType()
    }

    sealed class LongType : Operand() {
        data class ConstLong(val value: Long) : LongType()
        object RuntimeLong : LongType()
    }

    sealed class FloatType : Operand() {
        data class ConstFloat(val value: Float) : FloatType()
        object RuntimeFloat : FloatType()
    }

    sealed class DoubleType : Operand() {
        data class ConstDouble(val value: Double) : DoubleType()
        object RuntimeDouble : DoubleType()
    }

    sealed class ByteType : Operand() {
        data class ConstByte(val value: Int) : ByteType()
        object RuntimeByte : ByteType()
    }

    sealed class ShortType : Operand() {
        data class ConstShort(val value: Int) : ShortType()
        object RuntimeShort : ShortType()
    }

    sealed class CharType : Operand() {
        object RuntimeChar : CharType()
    }

    sealed class RefType : Operand() {
        data class ArrayRef(val type: Int) : RefType()
        object RefArrayRef : RefType()
        object RuntimeRef : RefType()
        object Null : RefType()
    }
}

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

internal sealed class Category {
    object CategoryOne : Category()
    object CategoryTwo : Category()
}

internal sealed class Operand {

    abstract fun category(): Category

    sealed class IntType : Operand() {

        override fun category() = Category.CategoryOne

        data class ConstInt(val value: Int) : IntType()
        object RuntimeInt : IntType()
    }

    sealed class LongType : Operand() {

        override fun category() = Category.CategoryTwo

        data class ConstLong(val value: Long) : LongType()
        object RuntimeLong : LongType()
    }

    sealed class FloatType : Operand() {

        override fun category() = Category.CategoryOne

        data class ConstFloat(val value: Float) : FloatType()
        object RuntimeFloat : FloatType()
    }

    sealed class DoubleType : Operand() {

        override fun category() = Category.CategoryTwo

        data class ConstDouble(val value: Double) : DoubleType()
        object RuntimeDouble : DoubleType()
    }

    sealed class ByteType : Operand() {

        override fun category() = Category.CategoryOne

        data class ConstByte(val value: Int) : ByteType()
        object RuntimeByte : ByteType()
    }

    sealed class ShortType : Operand() {

        override fun category() = Category.CategoryOne

        data class ConstShort(val value: Int) : ShortType()
        object RuntimeShort : ShortType()
    }

    sealed class CharType : Operand() {

        override fun category() = Category.CategoryOne

        object RuntimeChar : CharType()
    }

    sealed class RefType : Operand() {

        override fun category() = Category.CategoryOne

        data class ArrayRef(val type: Int) : RefType()
        object RefArrayRef : RefType()
        object RuntimeRef : RefType()
        object Null : RefType()
    }
}

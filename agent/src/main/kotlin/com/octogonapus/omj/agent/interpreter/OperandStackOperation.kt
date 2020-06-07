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

internal sealed class OperandStackOperation {
    object NOP : OperandStackOperation()
    data class PushConstInt(val value: Int) : OperandStackOperation()
    data class PushConstLong(val value: Long) : OperandStackOperation()
    data class PushConstFloat(val value: Float) : OperandStackOperation()
    data class PushConstDouble(val value: Double) : OperandStackOperation()
    data class PushConstByte(val value: Int) : OperandStackOperation()
    data class PushConstShort(val value: Int) : OperandStackOperation()
    data class NewArray(val type: Int) : OperandStackOperation()
    object PushNullRef : OperandStackOperation()
    object LoadIntFromArray : OperandStackOperation()
    object StoreIntoIntArray : OperandStackOperation()
    object LoadLongFromArray : OperandStackOperation()
    object StoreIntoLongArray : OperandStackOperation()
    object LoadFloatFromArray : OperandStackOperation()
    object StoreIntoFloatArray : OperandStackOperation()
    object LoadDoubleFromArray : OperandStackOperation()
    object StoreIntoDoubleArray : OperandStackOperation()
    object LoadRefFromArray : OperandStackOperation()
    object StoreIntoRefArray : OperandStackOperation()
    object LoadByteFromArray : OperandStackOperation()
    object StoreIntoByteArray : OperandStackOperation()
    object LoadCharFromArray : OperandStackOperation()
    object StoreIntoCharArray : OperandStackOperation()
    object LoadShortFromArray : OperandStackOperation()
    object StoreIntoShortArray : OperandStackOperation()
    object Pop : OperandStackOperation()
    object Pop2 : OperandStackOperation()
    object Dup : OperandStackOperation()
    object DupX1 : OperandStackOperation()
    object DupX2 : OperandStackOperation()
    object Dup2 : OperandStackOperation()
    object Dup2X1 : OperandStackOperation()
    object Dup2X2 : OperandStackOperation()
}

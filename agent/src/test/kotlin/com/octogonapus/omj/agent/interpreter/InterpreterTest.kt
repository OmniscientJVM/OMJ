package com.octogonapus.omj.agent.interpreter

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import io.kotest.property.exhaustive.ints
import io.kotest.property.exhaustive.merge
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
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.DUP2
import org.objectweb.asm.Opcodes.DUP2_X1
import org.objectweb.asm.Opcodes.DUP2_X2
import org.objectweb.asm.Opcodes.DUP_X1
import org.objectweb.asm.Opcodes.DUP_X2
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
import org.objectweb.asm.Opcodes.POP
import org.objectweb.asm.Opcodes.POP2
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
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.IntInsnNode
import java.lang.RuntimeException

class ArrayStoresAndLoads : StringSpec({
    "check store insns" {
        checkAll(arrayStoreInsns, nonNegInts) { storeInsn, index ->
            checkAll(valuesForArrayInsn(storeInsn)) { value ->
                singleInsnTest(
                        insn = InsnNode(storeInsn),
                        stackBefore = OperandStack.from(OperandUtil.operandForArrayInsn(storeInsn), index, value),
                        stackAfter = OperandStack.from()
                )
            }
        }
    }

    "check load insns" {
        checkAll(arrayLoadInsns, nonNegInts) { loadInsn, index ->
            singleInsnTest(
                    insn = InsnNode(loadInsn),
                    stackBefore = OperandStack.from(OperandUtil.operandForArrayInsn(loadInsn), index),
                    stackAfter = OperandStack.from(runtimeValueForArrayInsn(loadInsn))
            )
        }
    }
})

class Consts : StringSpec({
    "iconst" {
        checkAll(Exhaustive.ints(0..5)) { offset ->
            singleInsnTest(
                    insn = InsnNode(ICONST_M1 + offset),
                    stackAfter = OperandStack.from(Operand.IntType.ConstInt(-1 + offset))
            )
        }
    }

    "lconst" {
        checkAll(Exhaustive.ints(0..1)) { offset ->
            singleInsnTest(
                    insn = InsnNode(LCONST_0 + offset),
                    stackAfter = OperandStack.from(Operand.LongType.ConstLong(offset.toLong()))
            )
        }
    }

    "fconst" {
        checkAll(Exhaustive.ints(0..2)) { offset ->
            singleInsnTest(
                    insn = InsnNode(FCONST_0 + offset),
                    stackAfter = OperandStack.from(Operand.FloatType.ConstFloat(offset.toFloat()))
            )
        }
    }

    "dconst" {
        checkAll(Exhaustive.ints(0..1)) { offset ->
            singleInsnTest(
                    insn = InsnNode(DCONST_0 + offset),
                    stackAfter = OperandStack.from(Operand.DoubleType.ConstDouble(offset.toDouble()))
            )
        }
    }
})

class OtherInsns : StringSpec({
    "nop" {
        singleInsnTest(
                insn = InsnNode(NOP),
                stackAfter = OperandStack.from()
        )
    }

    "aconst_null" {
        singleInsnTest(
                insn = InsnNode(ACONST_NULL),
                stackAfter = OperandStack.from(Operand.RefType.Null)
        )
    }

    "bipush" {
        singleInsnTest(
                insn = IntInsnNode(BIPUSH, 7),
                stackAfter = OperandStack.from(Operand.ByteType.ConstByte(7))
        )
    }

    "sipush" {
        singleInsnTest(
                insn = IntInsnNode(SIPUSH, 12345),
                stackAfter = OperandStack.from(Operand.ShortType.ConstShort(12345))
        )
    }

    "newarray" {
        checkAll(allArrayTypes, positiveInts) { arrayType, count ->
            singleInsnTest(
                    insn = IntInsnNode(NEWARRAY, arrayType),
                    stackBefore = OperandStack.from(count),
                    stackAfter = OperandStack.from(Operand.RefType.ArrayRef(arrayType))
            )
        }
    }

    "pop a category 1 value" {
        checkAll(allCategory1Values) { value ->
            singleInsnTest(
                    insn = InsnNode(POP),
                    stackBefore = OperandStack.from(value),
                    stackAfter = OperandStack.from()
            )
        }
    }

    "pop a category 2 value" {
        checkAll(allCategory2Values) { value ->
            singleInsnShouldThrowTest<IllegalStateException>(
                    insn = InsnNode(POP),
                    stackBefore = OperandStack.from(value)
            )
        }
    }

    "pop2 a category 1 value" {
        checkAll(allCategory1Values) { value ->
            singleInsnShouldThrowTest<RuntimeException>(
                    insn = InsnNode(POP2),
                    stackBefore = OperandStack.from(value)
            )
        }
    }

    "pop2 two category 1 value" {
        checkAll(allCategory1Values) { value ->
            singleInsnTest(
                    insn = InsnNode(POP2),
                    stackBefore = OperandStack.from(value, value),
                    stackAfter = OperandStack.from()
            )
        }
    }

    "pop2 a category 2 value" {
        checkAll(allCategory2Values) { value ->
            singleInsnTest(
                    insn = InsnNode(POP2),
                    stackBefore = OperandStack.from(value),
                    stackAfter = OperandStack.from()
            )
        }
    }

    "dup a category 1 value" {
        checkAll(allCategory1Values) { value ->
            singleInsnTest(
                    insn = InsnNode(DUP),
                    stackBefore = OperandStack.from(value),
                    stackAfter = OperandStack.from(value, value)
            )
        }
    }

    "dup a category 2 value" {
        checkAll(allCategory2Values) { value ->
            singleInsnShouldThrowTest<RuntimeException>(
                    insn = InsnNode(DUP),
                    stackBefore = OperandStack.from(value)
            )
        }
    }

    "dup_x1 a category 1 value" {
        checkAll(allCategory1Values, allCategory1Values) { value1, value2 ->
            singleInsnTest(
                    insn = InsnNode(DUP_X1),
                    stackBefore = OperandStack.from(value2, value1),
                    stackAfter = OperandStack.from(value1, value2, value1)
            )
        }
    }

    "dup_x1 a category 1 value over a category 2 value" {
        checkAll(allCategory1Values, allCategory2Values) { value1, value2 ->
            singleInsnShouldThrowTest<RuntimeException>(
                    insn = InsnNode(DUP_X1),
                    stackBefore = OperandStack.from(value2, value1)
            )
            singleInsnShouldThrowTest<RuntimeException>(
                    insn = InsnNode(DUP_X1),
                    stackBefore = OperandStack.from(value1, value2)
            )
        }
    }

    "dup_x2 with three category 1 values" {
        checkAll(allCategory1Values, allCategory1Values, allCategory1Values) { value1, value2, value3 ->
            singleInsnTest(
                    insn = InsnNode(DUP_X2),
                    stackBefore = OperandStack.from(value3, value2, value1),
                    stackAfter = OperandStack.from(value1, value3, value2, value1)
            )
        }
    }

    "dup_x2 with a category 1 value and a category 2 value" {
        checkAll(allCategory1Values, allCategory2Values) { value1, value2 ->
            singleInsnTest(
                    insn = InsnNode(DUP_X2),
                    stackBefore = OperandStack.from(value2, value1),
                    stackAfter = OperandStack.from(value1, value2, value1)
            )
        }
    }

    "dup_x2 with a category 2 value and a category 1 value" {
        checkAll(allCategory2Values, allCategory1Values) { value1, value2 ->
            singleInsnShouldThrowTest<RuntimeException>(
                    insn = InsnNode(DUP_X2),
                    stackBefore = OperandStack.from(value2, value1)
            )
        }
    }

    "dup2 with category 1 values" {
        checkAll(allCategory1Values, allCategory1Values) { value1, value2 ->
            singleInsnTest(
                    insn = InsnNode(DUP2),
                    stackBefore = OperandStack.from(value2, value1),
                    stackAfter = OperandStack.from(value2, value1, value2, value1)
            )
        }
    }

    "dup2 with category 2 value" {
        checkAll(allCategory2Values) { value ->
            singleInsnTest(
                    insn = InsnNode(DUP2),
                    stackBefore = OperandStack.from(value),
                    stackAfter = OperandStack.from(value, value)
            )
        }
    }

    "dup2 with category 1 value and category 2 value" {
        checkAll(allCategory1Values, allCategory2Values) { value1, value2 ->
            singleInsnShouldThrowTest<RuntimeException>(
                    insn = InsnNode(DUP2),
                    stackBefore = OperandStack.from(value2, value1)
            )
        }
    }

    "dup2_x1 with category 1 values" {
        checkAll(allCategory1Values, allCategory1Values, allCategory1Values) { value1, value2, value3 ->
            singleInsnTest(
                    insn = InsnNode(DUP2_X1),
                    stackBefore = OperandStack.from(value3, value2, value1),
                    stackAfter = OperandStack.from(value2, value1, value3, value2, value1)
            )
        }
    }

    "dup2_x1 with category 2 value over category 1 value" {
        checkAll(allCategory2Values, allCategory1Values) { value1, value2 ->
            singleInsnTest(
                    insn = InsnNode(DUP2_X1),
                    stackBefore = OperandStack.from(value2, value1),
                    stackAfter = OperandStack.from(value1, value2, value1)
            )
        }
    }

    "dup2_x1 with category 1 value over category 2 value" {
        checkAll(allCategory1Values, allCategory2Values) { value1, value2 ->
            singleInsnShouldThrowTest<RuntimeException>(
                    insn = InsnNode(DUP2_X1),
                    stackBefore = OperandStack.from(value2, value1)
            )
        }
    }

    "dup2_x2 with category 1 values" {
        checkAll(allCategory1Values, allCategory1Values, allCategory1Values, allCategory1Values) { value1, value2, value3, value4 ->
            singleInsnTest(
                    insn = InsnNode(DUP2_X2),
                    stackBefore = OperandStack.from(value4, value3, value2, value1),
                    stackAfter = OperandStack.from(value2, value1, value4, value3, value2, value1)
            )
        }
    }

    "dup2_x2 with category 2 value over category 1 values" {
        checkAll(allCategory2Values, allCategory1Values, allCategory1Values) { value1, value2, value3 ->
            singleInsnTest(
                    insn = InsnNode(DUP2_X2),
                    stackBefore = OperandStack.from(value3, value2, value1),
                    stackAfter = OperandStack.from(value1, value3, value2, value1)
            )
        }
    }

    "dup2_x2 with category 1 values over category 2 value" {
        checkAll(allCategory1Values, allCategory1Values, allCategory2Values) { value1, value2, value3 ->
            singleInsnTest(
                    insn = InsnNode(DUP2_X2),
                    stackBefore = OperandStack.from(value3, value2, value1),
                    stackAfter = OperandStack.from(value2, value1, value3, value2, value1)
            )
        }
    }

    "dup2_x2 with category 2 value over category 2 value" {
        checkAll(allCategory2Values, allCategory2Values) { value1, value2 ->
            singleInsnTest(
                    insn = InsnNode(DUP2_X2),
                    stackBefore = OperandStack.from(value2, value1),
                    stackAfter = OperandStack.from(value1, value2, value1)
            )
        }
    }
})

internal val arrayLoadInsns =
        listOf(IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD).exhaustive()

internal val arrayStoreInsns =
        listOf(IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE).exhaustive()

internal val allArrayTypes =
        listOf(T_BOOLEAN, T_CHAR, T_FLOAT, T_DOUBLE, T_BYTE, T_SHORT, T_INT, T_LONG).exhaustive()

internal val positiveInts = listOf(
        Operand.IntType.ConstInt(1),
        Operand.IntType.ConstInt(2),
        Operand.IntType.ConstInt(3),
        Operand.IntType.ConstInt(4),
        Operand.IntType.ConstInt(5),
        Operand.IntType.RuntimeInt
).exhaustive()

internal val nonNegInts = listOf(
        Operand.IntType.ConstInt(0)
).exhaustive<Operand.IntType>().merge(positiveInts)

internal val allInts = listOf(
        Operand.IntType.ConstInt(-1)
).exhaustive<Operand.IntType>().merge(nonNegInts)

internal val allLongs = listOf(
        Operand.LongType.ConstLong(0),
        Operand.LongType.ConstLong(1),
        Operand.LongType.RuntimeLong
).exhaustive()

internal val allFloats = listOf(
        Operand.FloatType.ConstFloat(0.0f),
        Operand.FloatType.ConstFloat(1.0f),
        Operand.FloatType.RuntimeFloat
).exhaustive()

internal val allDoubles = listOf(
        Operand.DoubleType.ConstDouble(0.0),
        Operand.DoubleType.ConstDouble(1.0),
        Operand.DoubleType.RuntimeDouble
).exhaustive()

internal val allRefs = listOf(
        Operand.RefType.RefArrayRef,
        Operand.RefType.ArrayRef(T_INT),
        Operand.RefType.Null,
        Operand.RefType.RuntimeRef
).exhaustive()

internal val allBytes = listOf(
        Operand.ByteType.ConstByte(0),
        Operand.ByteType.ConstByte(1),
        Operand.ByteType.RuntimeByte
).exhaustive()

internal val allChars = listOf(
        Operand.CharType.RuntimeChar
).exhaustive<Operand.CharType>()

internal val allShorts = listOf(
        Operand.ShortType.ConstShort(0),
        Operand.ShortType.ConstShort(1),
        Operand.ShortType.RuntimeShort
).exhaustive()

internal val allCategory1Values = allInts.merge(allFloats).merge(allRefs).merge(allBytes)
        .merge(allChars).merge(allShorts)

internal val allCategory2Values = allLongs.merge(allDoubles)

fun <A, B : A, C : A> Exhaustive<B>.merge(other: Exhaustive<C>): Exhaustive<A> =
        object : Exhaustive<A>() {
    override val values: List<A> = this@merge.values.zip(other.values)
            .flatMap { listOf(it.first, it.second) }
}

/**
 * @return The values you could store in an array of the type corresponding to the [opcode].
 */
internal fun valuesForArrayInsn(opcode: Int) = when (opcode) {
    IALOAD, IASTORE -> allInts
    LALOAD, LASTORE -> allLongs
    FALOAD, FASTORE -> allFloats
    DALOAD, DASTORE -> allDoubles
    AALOAD, AASTORE -> allRefs
    BALOAD, BASTORE -> allBytes
    CALOAD, CASTORE -> allChars
    SALOAD, SASTORE -> allShorts
    else -> error("")
}

/**
 * @return The Runtime* values you could get from an array of the type corresponding to the
 * [opcode].
 */
internal fun runtimeValueForArrayInsn(opcode: Int) = when (opcode) {
    IALOAD -> Operand.IntType.RuntimeInt
    LALOAD -> Operand.LongType.RuntimeLong
    FALOAD -> Operand.FloatType.RuntimeFloat
    DALOAD -> Operand.DoubleType.RuntimeDouble
    AALOAD -> Operand.RefType.RuntimeRef
    BALOAD -> Operand.ByteType.RuntimeByte
    CALOAD -> Operand.CharType.RuntimeChar
    SALOAD -> Operand.ShortType.RuntimeShort
    else -> error("")
}

/**
 * Tests the mutations a single instruction does to the stack.
 *
 * @param insn The instruction to execute.
 * @param stackAfter The expected state of the stack after the instruction runs.
 * @param stackBefore The given initial state of the stack before the instruction runs. Pass null
 * for an empty stack.
 */
internal fun singleInsnTest(
        insn: AbstractInsnNode,
        stackAfter: OperandStack,
        stackBefore: OperandStack? = null
) {
    val insnList = if (stackBefore == null) {
        InsnList().apply {
            add(insn)
        }
    } else {
        InsnList().apply {
            add(InsnNode(NOP)) // Add a NOP so we can set the stack for it to stackBefore
            add(insn)
        }
    }

    val interpreter = Interpreter(insnList)

    // This will set the stack after the NOP to stackBefore
    if (stackBefore != null) interpreter.setStackAfter(insnList.first, stackBefore)

    interpreter.stackAfter(insn).shouldBe(stackAfter)
}

/**
 * Tests the mutations a single instruction does to the stack.
 *
 * @param insn The instruction to execute.
 * @param stackBefore The given initial state of the stack before the instruction runs. Pass null
 * for an empty stack.
 */
internal inline fun <reified T : Throwable> singleInsnShouldThrowTest(
        insn: AbstractInsnNode,
        stackBefore: OperandStack? = null
) {
    val insnList = if (stackBefore == null) {
        InsnList().apply {
            add(insn)
        }
    } else {
        InsnList().apply {
            add(InsnNode(NOP)) // Add a NOP so we can set the stack for it to stackBefore
            add(insn)
        }
    }

    val interpreter = Interpreter(insnList)

    // This will set the stack after the NOP to stackBefore
    if (stackBefore != null) interpreter.setStackAfter(insnList.first, stackBefore)

    shouldThrow<T> { interpreter.stackAfter(insn) }
}

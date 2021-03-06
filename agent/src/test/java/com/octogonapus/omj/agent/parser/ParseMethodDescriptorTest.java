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
package com.octogonapus.omj.agent.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ParseMethodDescriptorTest {

  @Test
  void testEmptyVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.emptyList(), 'V'),
        Parser.parseMethodDescriptor("()V"));
  }

  @Test
  void testByteVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('B'), 'V'),
        Parser.parseMethodDescriptor("(B)V"));
  }

  @Test
  void testCharVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('C'), 'V'),
        Parser.parseMethodDescriptor("(C)V"));
  }

  @Test
  void testDoubleVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('D'), 'V'),
        Parser.parseMethodDescriptor("(D)V"));
  }

  @Test
  void testFloatVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('F'), 'V'),
        Parser.parseMethodDescriptor("(F)V"));
  }

  @Test
  void testIntVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('I'), 'V'),
        Parser.parseMethodDescriptor("(I)V"));
  }

  @Test
  void testLongVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('J'), 'V'),
        Parser.parseMethodDescriptor("(J)V"));
  }

  @Test
  void testShortVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('S'), 'V'),
        Parser.parseMethodDescriptor("(S)V"));
  }

  @Test
  void testBooleanVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('Z'), 'V'),
        Parser.parseMethodDescriptor("(Z)V"));
  }

  @Test
  void testIntDoubleVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Arrays.asList('I', 'D'), 'V'),
        Parser.parseMethodDescriptor("(ID)V"));
  }

  @Test
  void testIntDoubleShort() {
    assertEquals(
        new ParsedMethodDescriptor(Arrays.asList('I', 'D'), 'S'),
        Parser.parseMethodDescriptor("(ID)S"));
  }

  @Test
  void testArrayIntVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('L'), 'V'),
        Parser.parseMethodDescriptor("([I)V"));
  }

  @Test
  void testArrayArrayIntVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('L'), 'V'),
        Parser.parseMethodDescriptor("([[I)V"));
  }

  @Test
  void testEmptyArrayArrayInt() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.emptyList(), 'L'),
        Parser.parseMethodDescriptor("()[[I"));
  }

  @Test
  void testRefVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('L'), 'V'),
        Parser.parseMethodDescriptor("(Ljava/lang/Object;)V"));
  }

  @Test
  void testEmptyRef() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.emptyList(), 'L'),
        Parser.parseMethodDescriptor("()Ljava/lang/Object;"));
  }

  @Test
  void testArrayRefVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('L'), 'V'),
        Parser.parseMethodDescriptor("([Ljava/lang/Object;)V"));
  }

  @Test
  void testArrayArrayRefVoid() {
    assertEquals(
        new ParsedMethodDescriptor(Collections.singletonList('L'), 'V'),
        Parser.parseMethodDescriptor("([[Ljava/lang/Object;)V"));
  }

  @Test
  void testEmpty() {
    assertThrows(IllegalStateException.class, () -> Parser.parseMethodDescriptor(""));
  }

  @Test
  void testBadStart() {
    assertThrows(IllegalStateException.class, () -> Parser.parseMethodDescriptor("[)V"));
  }

  @Test
  void testBadParameterListEndArray() {
    assertThrows(IllegalStateException.class, () -> Parser.parseMethodDescriptor("(D[I"));
  }

  @Test
  void testBadParameterListEnd() {
    assertThrows(IllegalStateException.class, () -> Parser.parseMethodDescriptor("(D(V"));
  }

  @Test
  void testExtraLongDescriptor() {
    assertThrows(IllegalStateException.class, () -> Parser.parseMethodDescriptor("(ID)B[I"));
  }
}

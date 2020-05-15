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

import org.junit.jupiter.api.Test;

class ParseFieldDescriptorTest {

  @Test
  void testByte() {
    assertEquals('B', Parser.parseFieldDescriptor("B"));
  }

  @Test
  void testChar() {
    assertEquals('C', Parser.parseFieldDescriptor("C"));
  }

  @Test
  void testDouble() {
    assertEquals('D', Parser.parseFieldDescriptor("D"));
  }

  @Test
  void testFloat() {
    assertEquals('F', Parser.parseFieldDescriptor("F"));
  }

  @Test
  void testInt() {
    assertEquals('I', Parser.parseFieldDescriptor("I"));
  }

  @Test
  void testLong() {
    assertEquals('J', Parser.parseFieldDescriptor("J"));
  }

  @Test
  void testShort() {
    assertEquals('S', Parser.parseFieldDescriptor("S"));
  }

  @Test
  void testBoolean() {
    assertEquals('Z', Parser.parseFieldDescriptor("Z"));
  }

  @Test
  void testVoid() {
    assertEquals('V', Parser.parseFieldDescriptor("V"));
  }

  @Test
  void testArrayInt() {
    assertEquals('L', Parser.parseFieldDescriptor("[I"));
  }

  @Test
  void testArrayArrayInt() {
    assertEquals('L', Parser.parseFieldDescriptor("[[I"));
  }

  @Test
  void testString() {
    assertEquals('L', Parser.parseFieldDescriptor("Ljava/lang/String;"));
  }

  @Test
  void testEmpty() {
    assertThrows(IllegalStateException.class, () -> Parser.parseFieldDescriptor(""));
  }

  @Test
  void testExtraLongDescriptor() {
    assertThrows(IllegalStateException.class, () -> Parser.parseFieldDescriptor("ID"));
  }

  @Test
  void testUnknown() {
    assertThrows(IllegalStateException.class, () -> Parser.parseFieldDescriptor("Q"));
  }
}

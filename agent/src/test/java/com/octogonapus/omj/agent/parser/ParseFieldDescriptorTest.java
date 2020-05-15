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

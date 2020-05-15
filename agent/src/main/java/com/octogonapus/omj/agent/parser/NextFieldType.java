package com.octogonapus.omj.agent.parser;

final class NextFieldType {

  char fieldType;
  int nextStartingIndex;

  NextFieldType(final char fieldType, final int nextStartingIndex) {
    this.fieldType = fieldType;
    this.nextStartingIndex = nextStartingIndex;
  }
}

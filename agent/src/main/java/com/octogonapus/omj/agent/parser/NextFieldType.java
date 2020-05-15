package com.octogonapus.omj.agent.parser;

class NextFieldType {

  char fieldType;
  int nextStartingIndex;

  NextFieldType(final char fieldType, final int nextStartingIndex) {
    this.fieldType = fieldType;
    this.nextStartingIndex = nextStartingIndex;
  }
}

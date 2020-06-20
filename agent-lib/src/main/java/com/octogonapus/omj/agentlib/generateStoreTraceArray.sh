# $1 like `int`
# $2 like `I`
filename="StoreTrace_array_$1.java"
echo "package com.octogonapus.omj.agentlib;

import java.io.IOException;
import java.io.OutputStream;

public class StoreTrace_array_$1 implements Trace {

  private final long index;
  private final $1[] array;
  private final int arrayIndex;
  private final $1 value;
  private final String className;
  private final int lineNumber;
  private final String variableName;

  public StoreTrace_array_$1(final long index,
                              final $1[] array,
                              final int arrayIndex,
                              final $1 value,
                              final String className,
                              final int lineNumber,
                              final String variableName) {
    this.index = index;
    this.array = array;
    this.arrayIndex = arrayIndex;
    this.value = value;
    this.className = className;
    this.lineNumber = lineNumber;
    this.variableName = variableName;
  }

  @Override
  public void serialize(final OutputStream outputStream) throws IOException {
    TraceUtil.writeArrayStoreTraceHeader(outputStream, className, index, lineNumber, variableName, array, arrayIndex);
    outputStream.write('$2');
    // TODO: Write value
  }
}" > "$filename" && git add "$filename"

mkdir -p "$1/src/main/java/com/agenttest/$1" &&
  touch "$1/build.gradle" &&
  echo "package com.agenttest.$1;

public class Main {

  public static void main(String[] args) {
    new Foo().with();
  }
}" > "$1/src/main/java/com/agenttest/$1/Main.java" &&
  echo "package com.agenttest.$1;

public class Foo {
  public void with() {}
}" > "$1/src/main/java/com/agenttest/$1/Foo.java"

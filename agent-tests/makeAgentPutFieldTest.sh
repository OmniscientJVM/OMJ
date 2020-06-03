filename="$1/src/main/java/com/agenttest/$1/Main.java"
mkdir -p "$1/src/main/java/com/agenttest/$1" &&
  touch "$1/build.gradle" &&
  echo "package com.agenttest.$1;

public class Main {

  $2

  public static void main(String[] args) {
    new Main().$3
  }
}" > "$filename" &&
  git add "$1"
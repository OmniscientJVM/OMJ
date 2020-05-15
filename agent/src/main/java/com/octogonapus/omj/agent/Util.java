package com.octogonapus.omj.agent;

import java.nio.file.Path;
import java.nio.file.Paths;

final class Util {

  /** The directory OMJ keeps all its data in. */
  static final Path cacheDir = Paths.get(System.getProperty("user.home"), ".OMJ");
}

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
package com.octogonapus.omj.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Util {

  /** The directory OMJ keeps all its data in. */
  public static final Path cacheDir = Paths.get(System.getProperty("user.home"), ".OMJ");

  /** The directory OMJ keeps trace files in. */
  public static Path getTraceDir() {
    return Paths.get(System.getProperty("agent-lib.trace-dir", cacheDir.toString()));
  }

  /** The file path to extract the agent lib Jar to. */
  public static Path getAgentLibJar() {
    return Paths.get(
        System.getProperty("agent-lib.jar-path", cacheDir.resolve("agent-lib-all.jar").toString()));
  }
}

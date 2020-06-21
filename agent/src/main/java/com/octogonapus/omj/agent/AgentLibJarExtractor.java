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
package com.octogonapus.omj.agent;

import com.octogonapus.omj.util.JarExtractor;
import java.io.File;
import java.io.IOException;

public final class AgentLibJarExtractor {

  /**
   * Extracts the agent-lib jar from our jar and into a file.
   *
   * @return The file the agent-lib jar is extracted into.
   * @throws IOException Any IO problems along the way. All fatal.
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static File extractJar() throws IOException {
    return JarExtractor.extractJar("agent-lib-all");
  }
}

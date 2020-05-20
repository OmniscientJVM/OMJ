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

import com.octogonapus.omj.util.Util;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public final class Agent {

  public static void premain(final String args, final Instrumentation instrumentation) {
    final DynamicClassDefiner dynamicClassDefiner =
        new DynamicClassDefiner(instrumentation, Util.cacheDir);

    instrumentation.addTransformer(new OMJClassFileTransformer(dynamicClassDefiner));

    try {
      // Extract the agent-lib jar from our jar and let the instrumented jvm load from it
      instrumentation.appendToSystemClassLoaderSearch(
          new JarFile(AgentLibJarExtractor.extractJar()));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
}

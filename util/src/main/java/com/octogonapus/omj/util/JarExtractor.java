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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class JarExtractor {

  private static final Map<String, File> resourceMap = new HashMap<>();

  /**
   * Extracts the agent-lib jar from our jar and into a file.
   *
   * @return The file the agent-lib jar is extracted into.
   * @throws IOException Any IO problems along the way. All fatal.
   */
  public static File extractJar(final String resourceName) throws IOException {
    try (final var agentLib = ClassLoader.getSystemResourceAsStream(resourceName)) {
      if (agentLib == null) {
        throw new IOException("Could not locate resource: " + resourceName);
      }

      // Extract the Jar and save it so that subsequent calls do not try to extract the Jar again.
      if (!resourceMap.containsKey(resourceName)) {
        final var file =
            Files.createTempFile(Util.getAgentLibJarDir(), resourceName + '_', ".jar").toFile();
        file.deleteOnExit();
        copyStreamToFile(file, agentLib);
        resourceMap.put(resourceName, file);
      }

      return resourceMap.get(resourceName);
    }
  }

  private static void copyStreamToFile(File file, InputStream lib) throws IOException {
    try (final var os = Files.newOutputStream(file.toPath())) {
      final var buffer = new byte[0xFFFF];
      int readBytes;
      while (true) {
        readBytes = lib.read(buffer);

        // -1 means end of stream
        if (readBytes == -1) {
          break;
        }

        os.write(buffer, 0, readBytes);
      }
    }
  }
}

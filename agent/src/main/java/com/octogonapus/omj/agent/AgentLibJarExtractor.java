package com.octogonapus.omj.agent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class AgentLibJarExtractor {

  /**
   * Extracts the agent-lib jar from our jar and into a file.
   *
   * @return The file the agent-lib jar is extracted into.
   * @throws IOException Any IO problems along the way. All fatal.
   */
  public static File extractJar() throws IOException {
    try (final var agentLib = ClassLoader.getSystemResourceAsStream("agent-lib-all")) {
      if (agentLib == null) {
        throw new IOException("Could not locate agent-lib-all resource.");
      }

      final var file = Util.cacheDir.resolve("agent-lib-all.jar").toFile();

      // The lib contents can change all the time so we should re-extract it each time
      file.deleteOnExit();
      if (file.exists() && !file.delete()) {
        throw new IOException("Failed to delete file " + file.getPath());
      } else if (!file.getParentFile().mkdirs() && !file.getParentFile().exists()) {
        throw new IOException("Failed to make parent dirs for file " + file.getPath());
      }

      copyStreamToFile(file, agentLib);

      return file;
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

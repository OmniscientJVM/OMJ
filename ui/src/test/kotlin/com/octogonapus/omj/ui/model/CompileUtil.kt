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
package com.octogonapus.omj.ui.model

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Runs the agent on a [jarUnderTest] and saves traces into the [traceDir].
 *
 * @param jarUnderTest The filename of the Jar to run under the agent.
 * @param traceDir The dir to save trace files into.
 */
fun runAgent(jarUnderTest: String, traceDir: Path) {
    val jarFile = Paths.get(System.getProperty("agent-test.jar-dir"))
            .resolve(jarUnderTest)
            .toFile()

    ProcessBuilder(
            Paths.get(System.getProperty("java.home"))
                    .resolve("bin")
                    .resolve("java")
                    .toAbsolutePath()
                    .toString(),
            "-Dagent-lib.jar-path=${traceDir.resolve("agent-lib-all.jar").toAbsolutePath()}",
            "-Dagent-lib.trace-dir=${traceDir.toAbsolutePath()}",
            "-Dagent.include-package=com/agenttest/[a-zA-Z0-9/]*",
            "-Dagent.exclude-package=",
            "-javaagent:${System.getProperty("agent.jar")}",
            "-jar",
            jarFile.absolutePath
    ).inheritIO().start().waitFor()
}

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
package com.octogonapus.omj.testutil

import arrow.core.Tuple3
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldBeEmpty
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path
import java.nio.file.Paths
import mu.KotlinLogging

object CompileUtil {

    private val logger = KotlinLogging.logger { }

    /**
     * Checks that the agent test did not encounter any errors.
     *
     * @param result The return value from [runAgentTest].
     */
    fun checkForAgentTestErrors(result: Tuple3<Int, String, String>) {
        val (exitCode, _, stdErr) = result
        exitCode.shouldBe(0)
        stdErr.shouldBeEmpty()
    }

    /**
     * Runs the agent on a [jarUnderTest] and saves traces into the [traceDir].
     *
     * @param jarUnderTest The filename of the Jar to run under the agent.
     * @param traceDir The dir to save trace files into.
     */
    fun runAgentTest(jarUnderTest: String, traceDir: Path): Tuple3<Int, String, String> {
        val jarFile = Paths.get(System.getProperty("agent-test.jar-dir"))
            .resolve(jarUnderTest)
            .toFile()

        val process = ProcessBuilder(
            Paths.get(System.getProperty("java.home"))
                .resolve("bin")
                .resolve("java")
                .toAbsolutePath()
                .toString(),
            "-Dagent-lib.jar-dir=${traceDir.toAbsolutePath()}",
            "-Dagent-lib.trace-dir=${traceDir.toAbsolutePath()}",
            "-Dagent.include-package=com/agenttest/[a-zA-Z0-9/]*",
            "-Dagent.exclude-package=",
            "-javaagent:${System.getProperty("agent.jar")}",
            "-jar",
            jarFile.absolutePath
        ).start()

        logger.debug { "Started the agent on process ${process.pid()}" }

        BufferedReader(InputStreamReader(process.inputStream)).useLines { procStdOut ->
            BufferedReader(InputStreamReader(process.errorStream)).useLines { procStdErr ->
                val exitCode = try {
                    process.waitFor()
                } catch (ex: InterruptedException) {
                    // An interruption means that the caller wants the command to be stopped
                    // immediately.
                    process.destroyForcibly()

                    @Suppress("TooGenericExceptionThrown")
                    throw RuntimeException("Forcibly destroyed the process ${process.pid()}.", ex)
                }

                val stdOutString = procStdOut.joinToString("\n")
                val stdErrString = procStdErr.joinToString("\n")

                logger.debug {
                    """
                    Finished running the agent in process ${process.pid()}
                    Exit code = $exitCode
                    std out:
                    $stdOutString
                    std err:
                    $stdErrString
                    """.trimIndent()
                }

                return Tuple3(exitCode, stdOutString, stdErrString)
            }
        }
    }
}

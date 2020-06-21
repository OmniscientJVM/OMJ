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
package com.octogonapus.omj.agent

import com.octogonapus.omj.di.OMJKoinContext
import com.octogonapus.omj.util.Util
import java.io.IOException
import java.lang.instrument.Instrumentation
import java.util.jar.JarFile
import kotlin.system.exitProcess
import mu.KotlinLogging
import org.koin.dsl.koinApplication
import org.koin.dsl.module

@Suppress("unused")
object Agent {

    @JvmStatic
    fun premain(args: String?, instrumentation: Instrumentation) {
        mainImpl(instrumentation)
    }

    @JvmStatic
    fun agentmain(args: String?, instrumentation: Instrumentation) {
        mainImpl(instrumentation)
    }

    private fun mainImpl(instrumentation: Instrumentation) {
        OMJKoinContext.koinApp = koinApplication {
            modules(
                module {
                    single { DynamicClassDefiner(instrumentation, Util.cacheDir) }
                    single { ClassFilter.createFromSystemProperties() }
                }
            )
        }

        instrumentation.addTransformer(OMJClassFileTransformer())

        try {
            // Extract the agent-lib jar from our jar and let the instrumented jvm load from it
            instrumentation.appendToSystemClassLoaderSearch(
                JarFile(AgentLibJarExtractor.extractJar())
            )
        } catch (e: IOException) {
            logger.error("Failed to append the agent lib jar to the system class loader.", e)
            exitProcess(1)
        }
    }

    private val logger = KotlinLogging.logger { }
}

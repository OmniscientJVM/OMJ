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
package com.octogonapus.omj.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure

class CopyAgentTestJarPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val targetJarTask = target.tasks.named("jar", Jar::class.java)

        targetJarTask.configure {
            archiveBaseName.set("agent-test_${target.name}")
            archiveVersion.set("") // So that test code doesn't need to worry about the version
            manifest {
                attributes(mapOf("Main-Class" to "com.agenttest.${target.name}.Main"))
            }
        }

        target.tasks.register("copyAgentTestJar", Copy::class.java) {
            dependsOn(targetJarTask)
            from({ targetJarTask.map { it.archiveFile } })
            into({ target.rootProject.buildDir.toPath().resolve("agent-test-jars") })
        }
    }
}

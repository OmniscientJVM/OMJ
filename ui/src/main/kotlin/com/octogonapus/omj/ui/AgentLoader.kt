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
package com.octogonapus.omj.ui

import com.octogonapus.omj.util.JarExtractor
import com.sun.tools.attach.AttachNotSupportedException
import com.sun.tools.attach.VirtualMachine
import java.io.IOException
import java.lang.management.ManagementFactory

object AgentLoader {

    fun loadAgent() {
        val name = ManagementFactory.getRuntimeMXBean().name
        val pid = name.substring(0, name.indexOf('@'))
        try {
            val virtualMachine = VirtualMachine.attach(pid)
            val agentJar = JarExtractor.extractJar("agent-all")
            virtualMachine.loadAgent(agentJar.absolutePath, null)
            virtualMachine.detach()
        } catch (e: AttachNotSupportedException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

fun main() {
    AgentLoader.loadAgent()
}

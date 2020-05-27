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

import java.util.regex.Pattern
import mu.KotlinLogging

class ClassFilter private constructor(
    private val includeFilter: Pattern,
    private val excludeFilter: Pattern
) {

    /**
     * @return True if the class with name [className] should be transformed.
     */
    fun shouldTransform(className: String) =
        includeFilter.matcher(className).matches() &&
            !excludeFilter.matcher(className).matches()

    companion object {

        private val logger = KotlinLogging.logger { }

        /**
         * Creates a [ClassFilter] by loading the include and exclude filters from the system
         * properties `agent.include-package` and `agent.exclude-package`.
         */
        fun createFromSystemProperties(): ClassFilter {
            val includeFilterString = System.getProperty("agent.include-package")
            val excludeFilterString = System.getProperty("agent.exclude-package")
            logger.debug {
                """
                includeFilterString = $includeFilterString
                excludeFilterString = $excludeFilterString
                """.trimIndent()
            }

            checkNotNull(includeFilterString) {
                "An include filter must be specified with agent.include-package"
            }
            checkNotNull(excludeFilterString) {
                "An exclude filter must be specified with agent.exclude-package"
            }

            return ClassFilter(
                Pattern.compile(includeFilterString),
                Pattern.compile(excludeFilterString)
            )
        }
    }
}

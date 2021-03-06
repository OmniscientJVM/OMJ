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

/**
 * This file cannot be in a package. That would prevent Gradle from loading it inside `plugins {}`
 * blocks.
 */
object Versions {
    const val omj = "0.1.0"

    const val spotlessPlugin = "3.29.0"
    const val testLoggerPlugin = "2.0.0"
    const val ktlintPlugin = "9.3.0"
    const val detektPlugin = "1.9.0"
    const val shadowPlugin = "5.2.0"
    const val javafxPlugin = "0.0.8"
    const val downloadPlugin = "4.0.4"

    const val kotlin = "1.3.72"
    const val ktlint = "0.37.2"
    const val junit = "5.6.+"
    const val kotest = "4.1.+"
    const val asm = "8.0.+"
    const val byteBuddy = "1.10.+"
    const val logback = "1.2.+"
    const val kotlinLogging = "1.7.+"
    const val koin = "2.1.+"
    const val mockk = "1.10.+"
    const val arrow = "0.10.+"

    const val jacocoTool = "0.8.5"
    const val gradleWrapper = "6.5.1"
}

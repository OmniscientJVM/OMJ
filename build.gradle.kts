import com.adarshr.gradle.testlogger.theme.ThemeType
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("com.diffplug.gradle.spotless") version "3.29.0"
    id("com.adarshr.test-logger") version "2.0.0"
    jacoco
    kotlin("jvm") version "1.3.72"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    id("io.gitlab.arturbosch.detekt") version "1.9.0"
}

allprojects {
    apply {
        plugin("com.diffplug.gradle.spotless")
        plugin("com.adarshr.test-logger")
    }

    group = "com.octogonapus"
    version = "0.1.0"

    repositories {
        mavenCentral()
    }

    // Configures the Jacoco tool version to be the same for all projects that have it applied.
    pluginManager.withPlugin("jacoco") {
        // If this project has the plugin applied, configure the tool version.
        jacoco {
            toolVersion = property("jacoco-tool.version") as String
        }
    }

    tasks.withType<Test> {
        testLogging {
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    testlogger {
        theme = ThemeType.STANDARD_PARALLEL
        showStandardStreams = true
    }

    spotless {
        kotlinGradle {
            ktlint(property("ktlint.version") as String)
            trimTrailingWhitespace()
        }
        freshmark {
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
        }
        format("extraneous") {
            target("src/**/*.fxml")
            trimTrailingWhitespace()
            indentWithSpaces(2)
            endWithNewline()
        }
    }
}

subprojects {
    apply {
        plugin("java-library")
        plugin("jacoco")
    }

    dependencies {
        // JUnit
        testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = property("junit.version") as String)
        testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = property("junit.version") as String)
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_14
        targetCompatibility = JavaVersion.VERSION_14
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isIncremental = true
    }

    tasks.test {
        useJUnitPlatform {
            filter {
                includeTestsMatching("*Test")
                includeTestsMatching("*Tests")
                includeTestsMatching("*Spec")
            }

            /*
            These tests just test performance and should not run in CI.
             */
            excludeTags("performance")

            /*
            These tests are too slow to run in CI.
             */
            excludeTags("slow")

            testLogging {
                events(
                        TestLogEvent.FAILED,
                        TestLogEvent.PASSED,
                        TestLogEvent.SKIPPED,
                        TestLogEvent.STARTED
                )
                displayGranularity = 0
                showExceptions = true
                showCauses = true
                showStackTraces = true
                exceptionFormat = TestExceptionFormat.FULL
                showStandardStreams = true
            }

            reports.junitXml.destination = file(rootProject.buildDir.toPath().resolve("test-results").resolve(project.name))
        }
    }

    tasks.withType<JacocoReport> {
        reports {
            html.isEnabled = true
            xml.isEnabled = true
            csv.isEnabled = false
        }
    }

    spotless {
        java {
            googleJavaFormat("1.8")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
            licenseHeaderFile(rootProject.rootDir.toPath().resolve("config").resolve("spotless").resolve("omj.license"))
        }
    }
}

// Kotlin projects
configure(listOf(project(":agent"), project(":ui"))) {
    apply {
        plugin("kotlin")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("io.gitlab.arturbosch.detekt")
    }

    repositories {
        jcenter {
            content {
                includeGroup("org.jetbrains.kotlinx")
            }
        }
    }

    dependencies {
        implementation(group = "org.jetbrains.kotlin", name = "kotlin-stdlib-jdk8", version = property("kotlin.version") as String)
        implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = property("kotlin.version") as String)

        testImplementation(group = "io.kotest", name = "kotest-assertions-core-jvm", version = property("kotest.version") as String)
        testImplementation(group = "io.kotest", name = "kotest-assertions-jvm", version = property("kotest.version") as String)
        testImplementation(group = "io.kotest", name = "kotest-property-jvm", version = property("kotest.version") as String)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    spotless {
        kotlin {
            ktlint(property("ktlint.version") as String)
            licenseHeaderFile(rootProject.rootDir.toPath().resolve("config").resolve("spotless").resolve("omj.license"))
        }
    }

    detekt {
        input = files("src/main/kotlin", "src/test/kotlin")
        parallel = true
        config = files(rootProject.rootDir.toPath().resolve("config").resolve("detekt").resolve("config.yml"))
    }
}

val jacocoRootReport by tasks.creating(JacocoReport::class) {
    group = "verification"
    val excludedProjects = listOf<Project>()
    val includedProjects = subprojects.filter { it !in excludedProjects }

    dependsOn(includedProjects.flatMap { it.tasks.withType(JacocoReport::class) } - this)

    val allSrcDirs = includedProjects.map { it.sourceSets.main.get().allSource.srcDirs }
    additionalSourceDirs.setFrom(allSrcDirs)
    sourceDirectories.setFrom(allSrcDirs)
    classDirectories.setFrom(includedProjects.map { it.sourceSets.main.get().output })
    executionData.setFrom(includedProjects.filter {
        File("${it.buildDir}/jacoco/test.exec").exists()
    }.flatMap { it.tasks.withType(JacocoReport::class).map { it.executionData } })

    reports {
        html.isEnabled = true
        xml.isEnabled = true
        csv.isEnabled = false
    }
}

tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
    version = "6.4.1"
}

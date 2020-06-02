import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.nio.file.Path

plugins {
    id("com.github.johnrengelman.shadow") version Versions.shadowPlugin
    id("application")
    id("org.openjfx.javafxplugin") version Versions.javafxPlugin
}

description = "The UI."

// A resolvable configuration to hold dependencies needed by the child JVM (running the agent during
// integration tests) at runtime.
val childJVMRuntimeOnly: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    implementation(project(":util"))
    implementation(project(":logging"))

    runtimeOnly(project(path = ":agent", configuration = "shadow"))

    testImplementation(project(":testUtil"))

    childJVMRuntimeOnly(
        group = "org.jacoco",
        name = "org.jacoco.agent",
        version = Versions.jacocoTool
    )
}

val agentJarName = "agent-all"
val agentJarAllResource: Path = buildDir.toPath().resolve(agentJarName)

val copyAgentShadowJarTask = tasks.register("copyAgentShadowJar", Copy::class.java) {
    dependsOn(":agent:shadowJar")
    from({ project(":agent").tasks.named("shadowJar") })
    into(buildDir)
    rename { agentJarName }
}

tasks.named("shadowJar", ShadowJar::class.java) {
    dependsOn(copyAgentShadowJarTask)
    from({ agentJarAllResource })
}

// JaCoCo stores the agent jar inside the jar they upload to maven, so we need to extract it before
// we can use it.
val jacocoAgentJar: Path = buildDir.toPath().resolve("jacocoagent.jar")
val extractJacocoAgentTask = tasks.register("extractJacocoAgent", Copy::class.java) {
    from({
        zipTree(
            childJVMRuntimeOnly.first {
                it.name == "org.jacoco.agent-${Versions.jacocoTool}.jar"
            }
        ).filter { it.name == "jacocoagent.jar" }.singleFile
    })
    into({ buildDir })
}

tasks.test {
    dependsOn(copyAgentShadowJarTask, extractJacocoAgentTask)
    dependsOn(project(":agent-tests").getTasksByName("copyAgentTestJar", true))

    val agentAllJarPath = buildDir.toPath().resolve("agent-all").toAbsolutePath()

    // The assertion is necessary according to Gradle
    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    jvmArgs = jvmArgs!! + listOf(
        "-Dagent-test.jar-dir=" + rootProject.buildDir.toPath().resolve("agent-test-jars"),
        "-Dagent.jar=$agentAllJarPath",
        "-Dagent-test.jacoco-jar=$jacocoAgentJar",
        "-Dagent-test.jacoco-dest-file=$buildDir/jacoco/test.exec"
    )
}

javafx {
    version = "14"
    modules = listOf("javafx.base", "javafx.controls")
}

application {
    mainClassName = "com.octogonapus.omj.ui.view.MainUI"
}

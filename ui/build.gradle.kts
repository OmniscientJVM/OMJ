import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.nio.file.Path

plugins {
    id("com.github.johnrengelman.shadow") version Versions.shadowPlugin
    id("application")
    id("org.openjfx.javafxplugin") version Versions.javafxPlugin
}

description = "The UI."

dependencies {
    // Other projects
    implementation(project(":util"))
    implementation(project(":logging"))
    runtimeOnly(project(path = ":agent", configuration = "shadow"))
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

tasks.test {
    dependsOn(copyAgentShadowJarTask)
    dependsOn(project(":agent-tests").getTasksByName("copyAgentTestJar", true))

    val agentAllJarPath = buildDir.toPath().resolve("agent-all").toAbsolutePath()

    // The assertion is necessary according to Gradle
    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    jvmArgs = jvmArgs!! + listOf(
        "-Dagent-test.jar-dir=" + rootProject.buildDir.toPath().resolve("agent-test-jars"),
        "-Dagent.jar=$agentAllJarPath"
    )
}

javafx {
    version = "14"
    modules = listOf("javafx.base", "javafx.controls")
}

application {
    mainClassName = "com.octogonapus.omj.ui.view.MainUI"
}

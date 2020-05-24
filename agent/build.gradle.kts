import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.nio.file.Path

plugins {
    id("com.github.johnrengelman.shadow") version Versions.shadowPlugin
}

description = "The Agent."

dependencies {
    // Bytecode instrumentation
    implementation(group = "org.ow2.asm", name = "asm", version = Versions.asm)
    implementation(group = "org.ow2.asm", name = "asm-util", version = Versions.asm)
    implementation(group = "net.bytebuddy", name = "byte-buddy", version = Versions.byteBuddy)

    // Other projects
    implementation(project(":agent-lib"))
    implementation(project(":util"))
    implementation(project(":logging"))

    // Need the agent (yes, THIS project) shadow jar at runtime to dynamically load the agent-lib
    testRuntimeOnly(project(path = ":agent", configuration = "shadow"))
}

tasks.jar {
    manifest {
        attributes(mapOf(
                "Premain-Class" to "com.octogonapus.omj.agent.Agent",
                "Can-Redefine-Classes" to "true",
                "Can-Retransform-Classes" to "true",
                "Can-Set-Native-Method-Prefix" to "true"
        ))
    }
}

val agentLibJarName = "agent-lib-all"
val agentLibAllJarResource: Path = buildDir.toPath().resolve(agentLibJarName)

val copyAgentLibShadowJarTask = tasks.register("copyAgentLibShadowJar", Copy::class.java) {
    dependsOn(":agent-lib:shadowJar")
    from({ project(":agent-lib").tasks.named("shadowJar") })
    into(buildDir)
    rename { agentLibJarName }
}

tasks.named("shadowJar", ShadowJar::class.java) {
    dependsOn(copyAgentLibShadowJarTask)
    from({ agentLibAllJarResource })
}

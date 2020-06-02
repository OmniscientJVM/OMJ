plugins {
    id("com.github.johnrengelman.shadow") version Versions.shadowPlugin
}

description = "The supporting library for the Agent."

dependencies {
    implementation(project(":logging"))
    implementation(project(":util"))
}

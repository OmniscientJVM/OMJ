plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
}

repositories {
    jcenter()
}

gradlePlugin {
    plugins {
        create("copyAgentTestJarPlugin") {
            id = "com.octogonapus.omj.copyAgentTestJarPlugin"
            implementationClass = "com.octogonapus.omj.plugin.CopyAgentTestJarPlugin"
        }
    }
}

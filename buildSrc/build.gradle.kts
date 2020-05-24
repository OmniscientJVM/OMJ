plugins {
    /*
     * Don't try to apply spotless or any other plugins to this project. Just format files manually.
     */
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

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

/**
 * Retrieves the [sourceSets][org.gradle.api.tasks.SourceSetContainer] extension.
 */
val org.gradle.api.Project.`sourceSets`: org.gradle.api.tasks.SourceSetContainer get() =
    (this as org.gradle.api.plugins.ExtensionAware).extensions.getByName("sourceSets") as org.gradle.api.tasks.SourceSetContainer

/**
 * Provides the existing [main][org.gradle.api.tasks.SourceSet] element.
 */
val org.gradle.api.tasks.SourceSetContainer.`main`: NamedDomainObjectProvider<org.gradle.api.tasks.SourceSet>
    get() = named<org.gradle.api.tasks.SourceSet>("main")

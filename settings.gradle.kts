plugins {
    id("com.gradle.enterprise") version "3.3.1"
}

rootProject.name = "OMJ"

include(":agent")
include(":agent-lib")
include(":di")
include(":logging")
include(":testUtil")
include(":ui")
include(":util")

apply {
    from("settings_agentTest.txt")
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

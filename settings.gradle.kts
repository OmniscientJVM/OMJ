plugins {
    id("com.gradle.enterprise") version "3.3.1"
}

rootProject.name = "OMJ"

include(":agent")
include(":agent-lib")
include(":logging")
include(":ui")
include(":util")

include(":agent-tests")
include(":agent-tests:booleanTrue")
include(":agent-tests:byte3c")
include(":agent-tests:charQ")
include(":agent-tests:constructorInt6")
include(":agent-tests:double1p2")
include(":agent-tests:float4p3")
include(":agent-tests:int42")
include(":agent-tests:long123456789123456789")
include(":agent-tests:noargs")
include(":agent-tests:objectStringArray")
include(":agent-tests:objectTestDataClass")
include(":agent-tests:short12345")
include(":agent-tests:staticBlockCallStaticMethod")
include(":agent-tests:stringHello")
include(":agent-tests:stringHelloNull1")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

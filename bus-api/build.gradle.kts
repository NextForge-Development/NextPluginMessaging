plugins {
    `java-library`
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

dependencies {
    api("org.redisson:redisson:3.50.0")
    api("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    compileOnly("org.slf4j:slf4j-api:2.0.13")
}

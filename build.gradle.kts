plugins {
    id("io.freefair.lombok") version "8.6" apply false
    `maven-publish`
}

allprojects {
    apply(plugin = "io.freefair.lombok")

    group = "dev.soldat"
    version = "1.0.0"
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                groupId = "gg.nextforge"
                val moduleName = project.name.removePrefix("bus-")
                artifactId = "pluginmessaging-$moduleName"
                version = project.version.toString()
            }
        }
        repositories {
            maven {
                name = "reposiliteRepositoryReleases"
                url = uri("https://repo.nextforge.gg/releases")
                credentials {
                    username = "REDACTED"
                    password = "REDACTED"
                }
            }
        }
    }
}

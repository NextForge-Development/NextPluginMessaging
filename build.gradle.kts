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

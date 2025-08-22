plugins { `java-library`; id("io.freefair.lombok") }

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

dependencies {
    api(project(":bus-api"))
    compileOnly("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.3.0-SNAPSHOT") // Für @Subscribe etc., falls genutzt
}

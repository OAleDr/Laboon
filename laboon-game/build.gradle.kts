plugins {
    id("java-library")
}

dependencies {
    implementation(project(":laboon-core"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}
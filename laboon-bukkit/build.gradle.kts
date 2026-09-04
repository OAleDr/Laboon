plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
}

group = "br.com.laboon"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    implementation(project(":laboon-core"))

    implementation("redis.clients:jedis:7.2.0")
}

java {
    toolchain {
        languageVersion.set(
            JavaLanguageVersion.of(25)
        )
    }
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveBaseName.set("Laboon-Bukkit")
        archiveClassifier.set("")

        destinationDirectory.set(
            file("../laboon-bukkit/servers/lobby-01/plugins")
        )
    }
}
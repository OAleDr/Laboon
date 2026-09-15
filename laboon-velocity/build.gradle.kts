plugins {
    id("java")
    id("com.gradleup.shadow") version "9.2.2"
}

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:4.1.1")

    annotationProcessor(
        "com.velocitypowered:velocity-api:4.1.1"
    )

    // Laboon Core
    implementation(project(":laboon-core"))

    // Redis
    implementation("redis.clients:jedis:7.2.0")

    // PostgreSQL
    implementation("org.postgresql:postgresql:42.7.7")

    // HikariCP
    implementation("com.zaxxer:HikariCP:7.0.2")
}

tasks {
    jar {
        enabled = false
    }

    shadowJar {
        archiveBaseName.set("Laboon-Velocity")
        archiveClassifier.set("")

        destinationDirectory.set(
            file("../laboon-velocity/servers/proxy/plugins")
        )
    }
}

tasks.register("deployVelocity") {
    dependsOn(tasks.shadowJar)

    doLast {
        println("Laboon-Velocity implantado em:")
        println(
            file("../laboon-velocity/servers/proxy/plugins/Laboon-Velocity.jar")
                .absolutePath
        )
    }
}
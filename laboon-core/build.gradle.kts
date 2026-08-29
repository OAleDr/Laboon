plugins {
    id("java-library")
}

dependencies {
    implementation("redis.clients:jedis:7.2.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

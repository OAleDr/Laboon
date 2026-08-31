plugins {
    id("java-library")
}

dependencies {
    implementation("redis.clients:jedis:7.2.0")
    implementation("org.yaml:snakeyaml:2.4")
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

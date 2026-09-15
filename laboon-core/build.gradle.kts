plugins {
    id("java-library")
}

dependencies {

    // Redis
    implementation("redis.clients:jedis:7.2.0")

    // PostgreSQL
    implementation("org.postgresql:postgresql:42.7.7")

    // Connection Pool
    implementation("com.zaxxer:HikariCP:7.0.2")

    // Config
    implementation("org.yaml:snakeyaml:2.4")

    // Adventure
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")

    // Testes
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testImplementation("org.mockito:mockito-core:5.19.0")
}

tasks.test {
    useJUnitPlatform()
}
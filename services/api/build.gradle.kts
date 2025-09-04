plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":shared"))

    // spring boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Postgres
    implementation("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // flyway db migrations
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")

    // hibernate validation
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")

    // S3 file storage - MinIO
    implementation("io.minio:minio:8.5.1")

    // JTA Transaction manager - Atomikos
    implementation("com.atomikos:transactions-jta:6.0.0:jakarta")
    implementation("com.atomikos:transactions-jdbc:6.0.0:jakarta")

    // jwt related
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // sugar
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // open api docs (swagger)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

plugins {
    java
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.3-shred"

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
    // spring boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Postgres
    implementation("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // hibernate validation
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")

    // S3 file storage - MinIO
    implementation("io.minio:minio:8.5.1")

    // JTA Transaction manager - Atomikos
    implementation("com.atomikos:transactions-jta:6.0.0:jakarta")
    implementation("com.atomikos:transactions-jdbc:6.0.0:jakarta")

    // sugar
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

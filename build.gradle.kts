import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.1.2"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.4.0"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
}

group = "no.nav.helse.flex"
version = "1.0"
description = "flex-fss-proxy"
java.sourceCompatibility = JavaVersion.VERSION_17

ext["okhttp3.version"] = "4.9.3" // Token-support tester trenger Mockwebserver.

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/navikt/maven-release")
    }

    maven {
        url = uri("https://maven.pkg.github.com/navikt/freg-security")
    }
}

val tokenSupportVersion = "3.1.0"
val logstashEncoderVersion = "7.3"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("org.slf4j:slf4j-api")
    implementation("org.aspectj:aspectjrt")
    implementation("org.aspectj:aspectjweaver")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("no.nav.security:token-validation-spring:$tokenSupportVersion")
    implementation("no.nav.security:token-client-spring:$tokenSupportVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    testImplementation("no.nav.security:token-validation-spring-test:$tokenSupportVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("PASSED", "FAILED", "SKIPPED")
    }
}

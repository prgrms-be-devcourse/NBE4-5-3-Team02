plugins {
    kotlin("jvm") version "1.9.25" // 자바, 코틀린 동시에 이용 가능
    kotlin("plugin.spring") version "1.9.25" // 코틀린용 스프링 플러그인
    kotlin("plugin.jpa") version "1.9.25" // JPA 프록시를 위해서 설정
    kotlin("kapt") version "1.9.25" // QueryDSL Q 클래스 생성을 위해 적용
    kotlin("plugin.noarg") version "1.9.25"  // no-arg 플러그인
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.25" // kotlin-allopen
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.snackoverflow"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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
    // Spring Boot Starter
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-quartz:3.2.0")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework.session:spring-session-core")

    // DNS 관련 설정
    implementation("io.netty:netty-resolver-dns:4.1.118.Final")

    // Database 관련
    runtimeOnly("com.h2database:h2")
    runtimeOnly("com.mysql:mysql-connector-j")

    // 테스트 관련
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("io.github.hakky54:logcaptor:2.10.1")

    // AWS S3 관련
    implementation("org.springframework.cloud:spring-cloud-starter-aws:2.2.6.RELEASE")

    // Redis 및 WebSocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Google API 관련
    implementation("com.google.api-client:google-api-client:2.7.2")
    implementation("com.google.http-client:google-http-client-gson:1.43.3")

    // 기타 유틸리티
    developmentOnly("org.springframework.boot:spring-boot-devtools") // 개발용
    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5") // 암호화
    implementation("net.nurigo:sdk:4.3.2") // SMS 인증
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // QueryDSL 코어 & JPA
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    implementation("com.querydsl:querydsl-core:5.0.0")

    // Kotlin용 Annotation Processor (Java 코드도 함께 처리)
    kapt("com.querydsl:querydsl-apt:5.0.0:jakarta")
    kapt("jakarta.annotation:jakarta.annotation-api")
    kapt("jakarta.persistence:jakarta.persistence-api")

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // kotlin test
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5")
    implementation("org.geolatte:geolatte-geom:1.9.0")
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.91.Final:osx-aarch_64")

    // kotlin logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
}

dependencyManagement { // Spring Cloud Version 명시 (Hoxton.SR6)
    imports {
        mavenBom ("org.springframework.cloud:spring-cloud-dependencies:Hoxton.SR6")
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

noArg {
    annotation("jakarta.persistence.Entity")  // JPA 엔티티에 no-arg 생성자 생성 (컴파일 시 자동으로 기본 생성자가 추가)
}

// Q 클래스 생성 경로 지정
sourceSets.main {
    kotlin.srcDir("build/generated/source/kapt/main")
}

kotlin {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-java-parameters"
        }
    }
}

allOpen {
    annotations("jakarta.persistence.Entity", "jakarta.persistence.MappedSuperclass", "jakarta.persistence.Embeddable")
}
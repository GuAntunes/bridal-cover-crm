plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "br.com.gustavoantunes"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot Starters
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc") // ← Spring Data JDBC
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator") // ← Health checks e métricas
	
	// Database
	implementation("org.postgresql:postgresql") // ← PostgreSQL Driver
	implementation("org.flywaydb:flyway-core") // ← Database Migration
	implementation("org.flywaydb:flyway-database-postgresql") // ← Flyway PostgreSQL Support
	
	// JSON Processing
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310") // ← LocalDateTime support
	
	// Kotlin
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	
	// API Documentation (Swagger/OpenAPI)
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
	implementation("io.swagger.parser.v3:swagger-parser:2.1.19") // ← OpenAPI YAML Parser
	
	// Test Dependencies
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("com.tngtech.archunit:archunit-junit5:1.4.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0") // ← Mockito Kotlin support
	testImplementation("org.testcontainers:postgresql") // ← Integration tests with PostgreSQL
	testImplementation("org.testcontainers:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

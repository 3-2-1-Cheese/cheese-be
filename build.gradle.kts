plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25" apply false
	kotlin("plugin.jpa") version "1.9.25" apply false
	kotlin("kapt") version "1.9.25" apply false
	id("org.springframework.boot") version "3.4.5" apply false
	id("io.spring.dependency-management") version "1.1.7"
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

allprojects {
	group = "com.hsmile.cheese321"
	version = "0.0.1-SNAPSHOT"
	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "io.spring.dependency-management")

	//의존성 버전 관리
	dependencyManagement {
		imports {
			mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.5")
		}
		dependencies {
			dependency("org.locationtech.jts:jts-core:1.19.0")
		}
	}

	if (name == "api") {
		apply(plugin = "org.springframework.boot")
		apply(plugin = "org.jetbrains.kotlin.plugin.spring")
	}

	if (name == "data") {
		apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
		apply(plugin = "org.jetbrains.kotlin.kapt")
	}

	dependencies {
		implementation("org.jetbrains.kotlin:kotlin-reflect")
		testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	}

	kotlin {
		compilerOptions {
			freeCompilerArgs.addAll("-Xjsr305=strict")
		}
	}
}
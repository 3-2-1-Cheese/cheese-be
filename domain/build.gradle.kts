plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":common"))

    api("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.postgresql:postgresql")
    implementation("org.hibernate.orm:hibernate-spatial:6.4.0.Final")
    implementation("org.flywaydb:flyway-core")

    // QueryDSL (옵션)
    // implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    // annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.h2database:h2") // 테스트용 인메모리 DB
}

// QueryDSL Q클래스 생성을 위한 설정 (옵션)
/*
val querydslDir = "$buildDir/generated/querydsl"

sourceSets {
    main {
        java.srcDir(querydslDir)
    }
}

tasks.withType<JavaCompile> {
    options.annotationProcessorGeneratedSourcesDirectory = file(querydslDir)
}

tasks.named("clean") {
    doLast {
        file(querydslDir).deleteRecursively()
    }
}
*/

tasks.getByName<Jar>("jar") {
    enabled = true
}
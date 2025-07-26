dependencyManagement {
    imports {
        mavenBom("com.google.cloud:spring-cloud-gcp-dependencies:4.9.0")
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":data"))
    
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.locationtech.jts:jts-core")
    
    // JWT 라이브러리
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    
    // Prometheus 메트릭
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    // GCS
    implementation("com.google.cloud:google-cloud-storage:2.30.1")
    implementation("com.google.cloud:google-cloud-core:2.25.0")

    // Spring Cloud GCP 자동 설정
//    implementation("org.springframework.cloud:spring-cloud-gcp-starter-storage")
    
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
}
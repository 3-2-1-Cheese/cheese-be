package com.hsmile.cheese321.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication(exclude = [
    DataSourceAutoConfiguration::class, //디비 임시제거
    SecurityAutoConfiguration::class, //인증 임시제거
    ManagementWebSecurityAutoConfiguration::class //헬스체크
])
@Import(
    // PersistenceConfig::class,  // DB 설정 - 나중에 추가
)
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
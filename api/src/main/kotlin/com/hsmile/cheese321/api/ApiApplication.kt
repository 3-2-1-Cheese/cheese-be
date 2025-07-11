package com.hsmile.cheese321.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = ["com.hsmile.cheese321"]
)
class ApiApplication

fun main(args: Array<String>) {
    runApplication<ApiApplication>(*args)
}
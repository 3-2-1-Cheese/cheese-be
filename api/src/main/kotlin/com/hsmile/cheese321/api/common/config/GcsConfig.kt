package com.hsmile.cheese321.api.common.config

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * GCS Storage Bean 수동 설정
 */
@Configuration
class GcsConfig {

    @Bean
    fun storage(
        @Value("\${app.gcs.project-id}") projectId: String
    ): Storage {
        return StorageOptions.newBuilder()
            .setProjectId(projectId)
            .build()
            .service
    }
}
package com.hsmile.cheese321.api.common.config

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * GCS Storage Bean 설정
 * TODO: 인프라팀 GCS 버킷 준비되면 app.gcs.enabled=true로 활성화
 */
@Configuration
class GcsConfig {

    /**
     * GCS 활성화시에만 실제 Storage Bean 생성
     */
    @Bean
    @ConditionalOnProperty(
        name = ["app.gcs.enabled"],
        havingValue = "true"
    )
    fun storage(
        @Value("\${app.gcs.project-id}") projectId: String
    ): Storage {
        return StorageOptions.newBuilder()
            .setProjectId(projectId)
            .build()
            .service
    }
}
package com.hsmile.cheese321.api.user.service

import com.hsmile.cheese321.api.user.dto.*
import com.hsmile.cheese321.data.user.entity.User
import com.hsmile.cheese321.data.user.repository.UserRepository
import com.hsmile.cheese321.data.user.repository.UserFavoritePhotoBoothRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 사용자 정보 관리 서비스
 */
@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val userFavoritePhotoBoothRepository: UserFavoritePhotoBoothRepository,
    private val objectMapper: ObjectMapper
) {

    /**
     * 사용자 프로필 조회
     */
    @Transactional(readOnly = true)
    fun getMyProfile(userId: String): UserProfileResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }

        val favoriteCount = userFavoritePhotoBoothRepository.countByUserId(userId)
        val keywords = parseKeywords(user.preferredKeywords)

        return UserProfileResponse(
            id = user.id,
            nickname = user.nickname,
            profileImageUrl = user.profileImageUrl,
            preferredKeywords = keywords,
            favoriteCount = favoriteCount
        )
    }

    /**
     * 선호 키워드 설정/업데이트
     */
    fun updatePreferredKeywords(userId: String, request: UpdatePreferredKeywordsRequest): PreferredKeywordsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }

        val keywordsJson = objectMapper.writeValueAsString(request.keywords)
        user.updatePreferredKeywords(keywordsJson)
        userRepository.save(user)

        return PreferredKeywordsResponse(keywords = request.keywords)
    }

    /**
     * 선호 키워드 조회
     */
    @Transactional(readOnly = true)
    fun getPreferredKeywords(userId: String): PreferredKeywordsResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("사용자를 찾을 수 없습니다: $userId") }

        val keywords = parseKeywords(user.preferredKeywords)
        return PreferredKeywordsResponse(keywords = keywords)
    }

    /**
     * JSON 키워드 문자열을 List<String>으로 파싱
     */
    private fun parseKeywords(keywordsJson: String?): List<String> {
        if (keywordsJson.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            val typeRef = object : TypeReference<List<String>>() {}
            objectMapper.readValue(keywordsJson, typeRef)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
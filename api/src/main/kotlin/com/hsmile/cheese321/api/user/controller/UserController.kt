package com.hsmile.cheese321.api.user.controller

import com.hsmile.cheese321.api.user.dto.*
import com.hsmile.cheese321.api.user.service.UserService
import com.hsmile.cheese321.api.user.service.FavoriteService
import com.hsmile.cheese321.api.user.spec.UserApi
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

/**
 * 사용자 정보 관리 컨트롤러
 */
@RestController
class UserController(
    private val userService: UserService,
    private val favoriteService: FavoriteService
) : UserApi {

    /**
     * 내 프로필 조회
     */
    override fun getMyProfile(@AuthenticationPrincipal userId: String): ResponseEntity<UserProfileResponse> {
        val profile = userService.getMyProfile(userId)
        return ResponseEntity.ok(profile)
    }

    /**
     * 선호 키워드 설정
     */
    override fun updatePreferredKeywords(
        @AuthenticationPrincipal userId: String,
        @Valid request: UpdatePreferredKeywordsRequest
    ): ResponseEntity<PreferredKeywordsResponse> {
        val response = userService.updatePreferredKeywords(userId, request)
        return ResponseEntity.ok(response)
    }

    /**
     * 선호 키워드 조회
     */
    override fun getPreferredKeywords(@AuthenticationPrincipal userId: String): ResponseEntity<PreferredKeywordsResponse> {
        val response = userService.getPreferredKeywords(userId)
        return ResponseEntity.ok(response)
    }

    /**
     * 관심 사진관 목록 조회
     */
    override fun getFavoritePhotoBooths(@AuthenticationPrincipal userId: String): ResponseEntity<FavoritePhotoBoothsResponse> {
        val response = favoriteService.getFavoritePhotoBooths(userId)
        return ResponseEntity.ok(response)
    }
}

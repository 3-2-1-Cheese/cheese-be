package com.hsmile.cheese321.api.spec

import com.hsmile.cheese321.api.dto.request.StudioSearchRequest
import com.hsmile.cheese321.api.dto.response.StudioResponse
import org.springframework.web.bind.annotation.*

// @Tag(name = "Studio", description = "사진관 API")
@RequestMapping("/api/v1/studios")
interface StudioApi {

    // @Operation(summary = "사진관 목록 조회")
    @GetMapping
    fun getStudios(): List<StudioResponse>
}
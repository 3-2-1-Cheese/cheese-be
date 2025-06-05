package com.hsmile.cheese321.api.controller

import com.hsmile.cheese321.api.dto.response.StudioResponse
import com.hsmile.cheese321.api.spec.StudioApi
import org.springframework.web.bind.annotation.RestController

@RestController
class StudioController : StudioApi {

    override fun getStudios(): List<StudioResponse> {
        // TODO: 임시 더미 데이터
        return listOf(
            StudioResponse("1", "인생네컷 강남점", "서울시 강남구"),
            StudioResponse("2", "포토이즘 홍대점", "서울시 마포구")
        )
    }
}
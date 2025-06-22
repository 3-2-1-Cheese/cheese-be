package com.hsmile.cheese321.api.photobooth

import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothResponse
import com.hsmile.cheese321.api.photobooth.dto.PhotoBoothDetailResponse
import com.hsmile.cheese321.api.photobooth.service.PhotoBoothService
import org.springframework.web.bind.annotation.RestController

@RestController
class PhotoBoothController(
    private val photoBoothService: PhotoBoothService
) : PhotoBoothApi {

    override fun getPhotoBooths(
        lat: Double,
        lng: Double,
        radius: Int?,
        region: String?,
        brand: String?
    ): List<PhotoBoothResponse> {
        return photoBoothService.getPhotoBooths(lat, lng, radius, region, brand)
    }

    override fun getPhotoBoothDetail(id: String): PhotoBoothDetailResponse =
        photoBoothService.getPhotoBoothDetail(id)
}
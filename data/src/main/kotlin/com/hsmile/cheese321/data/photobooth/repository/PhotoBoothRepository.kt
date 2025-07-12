package com.hsmile.cheese321.data.photobooth.repository

import com.hsmile.cheese321.data.photobooth.entity.PhotoBooth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 사진관 기본 CRUD Repository
 */
@Repository
interface PhotoBoothRepository : JpaRepository<PhotoBooth, String> {

    // 현재 사용 중인 메서드 (PhotoBoothService.getPhotoBoothDetail에서 사용)
    // findByIdOrNull은 Spring Data JPA 확장 함수로 자동 제공됨

    // TODO: 나중에 필요할 때 추가할 메서드들 (현재 미사용)
    /*
    fun findByRegion(region: String): List<PhotoBooth>
    fun findByBrand(brand: String): List<PhotoBooth>
    fun findByRegionAndBrand(region: String, brand: String): List<PhotoBooth>
    */
}
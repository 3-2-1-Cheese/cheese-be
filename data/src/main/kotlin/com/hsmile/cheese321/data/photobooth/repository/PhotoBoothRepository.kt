package com.hsmile.cheese321.data.photobooth.repository

import com.hsmile.cheese321.data.photobooth.entity.PhotoBooth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 사진관 기본 CRUD Repository
 */
@Repository
interface PhotoBoothRepository : JpaRepository<PhotoBooth, String> {

    /**
     * QR 스캔용: 이름에 특정 문자열을 포함하고 브랜드가 일치하는 사진관 조회
     */
    fun findByNameContainingAndBrand(name: String, brand: String): List<PhotoBooth>

    /**
     * 브랜드별 사진관 조회
     */
    fun findByBrand(brand: String): List<PhotoBooth>
}
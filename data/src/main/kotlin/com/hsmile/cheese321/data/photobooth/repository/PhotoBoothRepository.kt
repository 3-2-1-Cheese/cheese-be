package com.hsmile.cheese321.data.photobooth.repository

import com.hsmile.cheese321.data.photobooth.entity.PhotoBooth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PhotoBoothRepository : JpaRepository<PhotoBooth, String> {
    fun findByRegion(region: String): List<PhotoBooth>
    fun findByBrand(brand: String): List<PhotoBooth>
    fun findByRegionAndBrand(region: String, brand: String): List<PhotoBooth>
}
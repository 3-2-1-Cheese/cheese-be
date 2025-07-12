package com.hsmile.cheese321.data.photobooth.repository

import com.hsmile.cheese321.data.photobooth.entity.PhotoBooth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * 사진관 기본 CRUD Repository
 */
@Repository
interface PhotoBoothRepository : JpaRepository<PhotoBooth, String>
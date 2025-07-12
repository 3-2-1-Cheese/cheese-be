// data/src/main/kotlin/com/hsmile/cheese321/data/album/entity/Album.kt

package com.hsmile.cheese321.data.album.entity

import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

/**
 * 앨범 엔티티
 * 사용자가 만든 사진첩 관리
 */
@Entity
@Table(name = "albums")
@EntityListeners(AuditingEntityListener::class)
class Album(
    @Id
    val id: String = UUID.randomUUID().toString(),

    /**
     * 앨범 소유자 ID
     */
    @Column(name = "user_id", nullable = false)
    val userId: String,

    /**
     * 앨범 이름
     */
    @Column(name = "name", nullable = false)
    var name: String,

    /**
     * 앨범 설명
     */
    @Column(name = "description")
    var description: String? = null,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    // TODO: 나중에 구현할 기능들
    // - 커버 사진 설정 (coverPhotoId)
    // - 앨범 공개/비공개 설정
    // - 앨범 공유 기능
) {

    /**
     * 앨범 정보 업데이트
     */
    fun updateInfo(name: String, description: String?) {
        this.name = name
        this.description = description
    }

    /**
     * 커버 사진 설정
     */
//    fun setCoverPhoto(photoId: String) {
//        this.coverPhotoId = photoId
//    }
}
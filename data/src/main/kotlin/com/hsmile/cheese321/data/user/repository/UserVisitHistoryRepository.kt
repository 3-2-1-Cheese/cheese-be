package com.hsmile.cheese321.data.user.repository

import com.hsmile.cheese321.data.user.entity.UserVisitHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * 사용자 방문 기록 데이터 접근 Repository
 */
@Repository
interface UserVisitHistoryRepository : JpaRepository<UserVisitHistory, String> {

    /**
     * 사용자의 방문 기록 조회 (최신순)
     */
    fun findByUserIdOrderByVisitedAtDesc(userId: String): List<UserVisitHistory>

    /**
     * 사용자의 최근 N개 방문 기록 조회
     */
    @Query("""
        SELECT vh FROM UserVisitHistory vh 
        WHERE vh.userId = :userId 
        ORDER BY vh.visitedAt DESC 
        LIMIT :limit
    """)
    fun findRecentVisitsByUserId(userId: String, limit: Int = 10): List<UserVisitHistory>

    /**
     * 특정 사용자의 특정 사진관 방문 기록 조회
     */
    fun findByUserIdAndPhotoBoothId(userId: String, photoBoothId: String): UserVisitHistory?

    /**
     * 특정 사용자의 특정 사진관 방문 기록 존재 여부 확인
     */
    fun existsByUserIdAndPhotoBoothId(userId: String, photoBoothId: String): Boolean

    /**
     * 사용자의 방문 기록 개수 조회
     */
    fun countByUserId(userId: String): Long

    /**
     * 사용자의 가장 오래된 방문 기록 조회 (FIFO 삭제용)
     */
    @Query("""
        SELECT vh FROM UserVisitHistory vh 
        WHERE vh.userId = :userId 
        ORDER BY vh.visitedAt ASC 
        LIMIT 1
    """)
    fun findOldestVisitByUserId(userId: String): UserVisitHistory?

    /**
     * 사용자의 가장 오래된 N개 방문 기록 조회 (대량 정리용)
     */
    @Query("""
        SELECT vh FROM UserVisitHistory vh 
        WHERE vh.userId = :userId 
        ORDER BY vh.visitedAt ASC 
        LIMIT :count
    """)
    fun findOldestVisitsByUserId(userId: String, count: Int): List<UserVisitHistory>

    /**
     * 특정 방문 기록들 삭제
     */
    @Modifying
    @Query("DELETE FROM UserVisitHistory vh WHERE vh.id IN :ids")
    fun deleteByIds(ids: List<String>): Int
}
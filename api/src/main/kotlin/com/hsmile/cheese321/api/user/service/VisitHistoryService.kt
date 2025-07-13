package com.hsmile.cheese321.api.user.service

import com.hsmile.cheese321.api.user.dto.RecentVisitsResponse
import com.hsmile.cheese321.api.user.dto.VisitHistoryInfo
import com.hsmile.cheese321.data.user.entity.UserVisitHistory
import com.hsmile.cheese321.data.user.repository.UserVisitHistoryRepository
import com.hsmile.cheese321.data.photobooth.repository.PhotoBoothRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.core.type.TypeReference
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 사용자 방문 기록 관리 서비스
 */
@Service
@Transactional
class VisitHistoryService(
    private val visitHistoryRepository: UserVisitHistoryRepository,
    private val photoBoothRepository: PhotoBoothRepository,
    private val objectMapper: ObjectMapper
) {

    companion object {
        private const val MAX_VISIT_HISTORY = 10 // 최대 보관할 방문 기록 수
    }

    /**
     * 방문 기록 추가 또는 업데이트 (FIFO 방식)
     * 독립적인 트랜잭션으로 실행하여 메인 기능(사진 업로드) 실패에 영향 없음
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun recordVisit(userId: String, photoBoothId: String) {
        try {
            // 1. 기존 방문 기록 확인
            val existingVisit = visitHistoryRepository.findByUserIdAndPhotoBoothId(userId, photoBoothId)

            if (existingVisit != null) {
                // 재방문: 시간만 업데이트
                existingVisit.updateVisitTime()
                visitHistoryRepository.save(existingVisit)
            } else {
                // 새 방문: 새 기록 생성
                val newVisit = UserVisitHistory(
                    userId = userId,
                    photoBoothId = photoBoothId,
                    visitedAt = LocalDateTime.now()
                )
                visitHistoryRepository.save(newVisit)

                // FIFO 관리: 10개 초과 시 가장 오래된 것 삭제
                maintainVisitHistoryLimit(userId)
            }
        } catch (e: Exception) {
            // 방문 기록 실패 시 로그만 남기고 메인 기능에 영향 주지 않음
            // TODO: 실제 운영 시 로깅 시스템 사용
            println("방문 기록 저장 실패 - userId: $userId, photoBoothId: $photoBoothId, error: ${e.message}")
        }
    }

    /**
     * 최근 방문 사진관 목록 조회
     */
    @Transactional(readOnly = true)
    fun getRecentVisits(userId: String): RecentVisitsResponse {
        val visits = visitHistoryRepository.findRecentVisitsByUserId(userId, MAX_VISIT_HISTORY)
        val totalCount = visitHistoryRepository.countByUserId(userId)

        if (visits.isEmpty()) {
            return RecentVisitsResponse(
                visits = emptyList(),
                totalCount = totalCount
            )
        }

        // 사진관 정보 한번에 조회 (N+1 방지)
        val photoBoothIds = visits.map { it.photoBoothId }
        val photoBooths = photoBoothRepository.findAllById(photoBoothIds)
            .associateBy { it.id }

        val visitInfos = visits.mapNotNull { visit ->
            val photoBooth = photoBooths[visit.photoBoothId] ?: return@mapNotNull null
            val imageUrls = parseImageUrls(photoBooth.imageUrls)

            VisitHistoryInfo(
                photoBoothId = photoBooth.id,
                photoBoothName = photoBooth.name,
                brand = photoBooth.brand,
                region = photoBooth.region,
                address = photoBooth.address,
                imageUrl = imageUrls.firstOrNull(),
                lastVisitedAt = visit.visitedAt.toString(),
                visitCount = 1 // 현재는 단순히 1로 고정
            )
        }

        return RecentVisitsResponse(
            visits = visitInfos,
            totalCount = totalCount
        )
    }

    /**
     * 방문 기록 개수 제한 유지 (FIFO)
     */
    private fun maintainVisitHistoryLimit(userId: String) {
        val currentCount = visitHistoryRepository.countByUserId(userId)

        if (currentCount > MAX_VISIT_HISTORY) {
            // 초과된 개수만큼 오래된 기록 삭제
            val excessCount = (currentCount - MAX_VISIT_HISTORY).toInt()
            val oldestVisits = visitHistoryRepository.findOldestVisitsByUserId(userId, excessCount)
            val idsToDelete = oldestVisits.map { it.id }

            if (idsToDelete.isNotEmpty()) {
                visitHistoryRepository.deleteByIds(idsToDelete)
            }
        }
    }

    /**
     * JSONB 이미지 URL 문자열을 List로 파싱
     */
    private fun parseImageUrls(imageUrls: String?): List<String> {
        if (imageUrls.isNullOrBlank()) {
            return emptyList()
        }

        return try {
            val typeRef = object : TypeReference<List<String>>() {}
            objectMapper.readValue(imageUrls, typeRef)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
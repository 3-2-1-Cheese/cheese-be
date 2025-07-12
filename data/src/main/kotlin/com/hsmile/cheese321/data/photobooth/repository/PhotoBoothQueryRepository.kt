package com.hsmile.cheese321.data.photobooth.repository

import com.hsmile.cheese321.data.photobooth.entity.PhotoBooth
import org.springframework.stereotype.Repository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

/**
 * 사진관 복잡 쿼리 처리용 Repository
 * PostGIS 공간 함수를 활용한 위치 기반 검색 제공
 */
@Repository
class PhotoBoothQueryRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    /**
     * 다중 조건으로 사진관 검색 (현재 사용 중)
     * @param lat 사용자 위도
     * @param lng 사용자 경도
     * @param radius 검색 반경(미터)
     * @param region 지역 필터 (nullable)
     * @param brand 브랜드 필터 (nullable)
     * @param keyword 통합 검색 키워드 (nullable)
     * @return 거리순 정렬된 필터링된 사진관 목록
     */
    fun search(
        lat: Double,
        lng: Double,
        radius: Int,
        region: String?,
        brand: String?,
        keyword: String?
    ): List<PhotoBooth> {

        val conditions = mutableListOf<String>()
        val params = mutableMapOf<String, Any>()

        // 기본 위치 조건 (필수)
        conditions.add("""
            ST_DWithin(
                p.location, 
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), 
                :radius
            )
        """.trimIndent())

        params["lat"] = lat
        params["lng"] = lng
        params["radius"] = radius

        // 지역 필터 (선택)
        region?.let {
            conditions.add("p.region = :region")
            params["region"] = it
        }

        // 브랜드 필터 (선택)
        brand?.let {
            conditions.add("p.brand = :brand")
            params["brand"] = it
        }

        // 키워드 검색 (선택) - 사진관명, 브랜드명, 지역에서 검색
        keyword?.let {
            conditions.add("(p.name ILIKE :keyword OR p.brand ILIKE :keyword OR p.region ILIKE :keyword)")
            params["keyword"] = "%$it%"
        }

        val sql = """
            SELECT p.* 
            FROM photobooths p 
            WHERE ${conditions.joinToString(" AND ")}
            ORDER BY ST_Distance(
                p.location, 
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
            )
        """.trimIndent()

        val query = entityManager.createNativeQuery(sql, PhotoBooth::class.java)
        params.forEach { (key, value) ->
            query.setParameter(key, value)
        }

        @Suppress("UNCHECKED_CAST")
        return query.resultList as List<PhotoBooth>
    }

    /**
     * 위치 정보 없이 지역/브랜드/키워드로만 사진관 검색
     * @param region 지역 필터 (nullable)
     * @param brand 브랜드 필터 (nullable)
     * @param keyword 통합 검색 키워드 (nullable)
     * @return 리뷰순 정렬된 사진관 목록
     */
    fun searchWithoutLocation(region: String?, brand: String?, keyword: String?): List<PhotoBooth> {
        val conditions = mutableListOf<String>()
        val params = mutableMapOf<String, Any>()

        // 지역 필터
        region?.let {
            conditions.add("p.region = :region")
            params["region"] = it
        }

        // 브랜드 필터
        brand?.let {
            conditions.add("p.brand = :brand")
            params["brand"] = it
        }

        // 키워드 검색 - 사진관명, 브랜드명, 지역에서 검색
        keyword?.let {
            conditions.add("(p.name ILIKE :keyword OR p.brand ILIKE :keyword OR p.region ILIKE :keyword)")
            params["keyword"] = "%$it%"
        }

        // 조건이 없으면 전체 조회
        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE ${conditions.joinToString(" AND ")}"
        } else {
            ""
        }

        val sql = """
            SELECT p.* 
            FROM photobooths p 
            $whereClause
            ORDER BY p.review_count DESC, p.name
        """.trimIndent()

        val query = entityManager.createNativeQuery(sql, PhotoBooth::class.java)
        params.forEach { (key, value) ->
            query.setParameter(key, value)
        }

        @Suppress("UNCHECKED_CAST")
        return query.resultList as List<PhotoBooth>
    }
}
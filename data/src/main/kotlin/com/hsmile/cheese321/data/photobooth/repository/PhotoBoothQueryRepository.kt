package com.hsmile.cheese321.data.photobooth.repository

import com.hsmile.cheese321.data.photobooth.entity.PhotoBooth
import org.springframework.stereotype.Repository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

/**
 * 사진관 복잡 쿼리 처리용 Repository
 */
@Repository
class PhotoBoothQueryRepository {

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    /**
     * 다중 조건으로 사진관 검색
     */
    fun search(
        lat: Double,
        lng: Double,
        radius: Int,
        region: String?,
        brand: String?,
        keyword: String?,
        sort: String = "distance"
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

        // 키워드 검색 (선택)
        keyword?.let {
            conditions.add("(p.name ILIKE :keyword OR p.brand ILIKE :keyword OR p.region ILIKE :keyword)")
            params["keyword"] = "%$it%"
        }

        // 정렬 조건 결정
        val orderBy = when (sort) {
            "popularity" -> {
                """
                ORDER BY (
                    COALESCE(
                        (SELECT AVG(CAST(r.rating AS decimal)) 
                         FROM photobooth_ratings r 
                         WHERE r.photo_booth_id = p.id), 0
                    ) * 0.7 + 
                    COALESCE(
                        (SELECT COUNT(*) 
                         FROM photobooth_ratings r 
                         WHERE r.photo_booth_id = p.id), 0
                    ) * 0.1
                ) DESC, p.name
                """.trimIndent()
            }
            else -> { // "distance" 또는 기타
                """
                ORDER BY ST_Distance(
                    p.location, 
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
                )
                """.trimIndent()
            }
        }

        val sql = """
            SELECT p.* 
            FROM photobooths p 
            WHERE ${conditions.joinToString(" AND ")}
            $orderBy
        """.trimIndent()

        val query = entityManager.createNativeQuery(sql, PhotoBooth::class.java)
        params.forEach { (key, value) ->
            query.setParameter(key, value)
        }

        @Suppress("UNCHECKED_CAST")
        return query.resultList as List<PhotoBooth>
    }

    /**
     * 위치 정보 없이 지역/브랜드/키워드로만 사진관 검색 (별점 기반 정렬로 수정)
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

        // 키워드 검색
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

        // 별점 기반 인기순 정렬
        val sql = """
            SELECT p.* 
            FROM photobooths p 
            $whereClause
            ORDER BY (
                COALESCE(
                    (SELECT AVG(CAST(r.rating AS decimal)) 
                     FROM photobooth_ratings r 
                     WHERE r.photo_booth_id = p.id), 0
                ) * 0.7 + 
                COALESCE(
                    (SELECT COUNT(*) 
                     FROM photobooth_ratings r 
                     WHERE r.photo_booth_id = p.id), 0
                ) * 0.1
            ) DESC, p.name
        """.trimIndent()

        val query = entityManager.createNativeQuery(sql, PhotoBooth::class.java)
        params.forEach { (key, value) ->
            query.setParameter(key, value)
        }

        @Suppress("UNCHECKED_CAST")
        return query.resultList as List<PhotoBooth>
    }
}
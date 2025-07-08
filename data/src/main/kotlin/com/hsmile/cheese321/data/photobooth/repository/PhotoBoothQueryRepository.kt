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
     * 특정 위치 주변의 사진관 검색
     * @param lat 중심점 위도
     * @param lng 중심점 경도
     * @param radius 검색 반경(미터)
     * @return 거리순 정렬된 사진관 목록
     */
    fun findNearby(lat: Double, lng: Double, radius: Int): List<PhotoBooth> {
        val sql = """
            SELECT p.* 
            FROM photobooths p 
            WHERE ST_DWithin(
                p.location, 
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326), 
                :radius
            )
            ORDER BY ST_Distance(
                p.location, 
                ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
            )
        """.trimIndent()

        return entityManager.createNativeQuery(sql, PhotoBooth::class.java)
            .setParameter("lat", lat)
            .setParameter("lng", lng)
            .setParameter("radius", radius)
            .resultList as List<PhotoBooth>
    }

    /**
     * 다중 조건으로 사진관 검색
     * @param lat 사용자 위도
     * @param lng 사용자 경도
     * @param radius 검색 반경(미터)
     * @param region 지역 필터 (nullable)
     * @param brand 브랜드 필터 (nullable)
     * @return 거리순 정렬된 필터링된 사진관 목록
     */
    fun search(
        lat: Double,
        lng: Double,
        radius: Int,
        region: String?,
        brand: String?
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
     * 브랜드별 사진관 조회
     * @param brand 브랜드명
     * @return 이름순 정렬된 사진관 목록
     */
    fun findByBrand(brand: String): List<PhotoBooth> {
        val sql = """
            SELECT p.* 
            FROM photobooths p 
            WHERE p.brand = :brand
            ORDER BY p.name
        """.trimIndent()

        return entityManager.createNativeQuery(sql, PhotoBooth::class.java)
            .setParameter("brand", brand)
            .resultList as List<PhotoBooth>
    }

    /**
     * 지역별 사진관 조회
     * @param region 지역명
     * @return 리뷰수 많은 순으로 정렬된 사진관 목록
     */
    fun findByRegion(region: String): List<PhotoBooth> {
        val sql = """
            SELECT p.* 
            FROM photobooths p 
            WHERE p.region = :region
            ORDER BY p.review_count DESC, p.name
        """.trimIndent()

        return entityManager.createNativeQuery(sql, PhotoBooth::class.java)
            .setParameter("region", region)
            .resultList as List<PhotoBooth>
    }
}
package com.hsmile.cheese321.data.photobooth.repository

import com.hsmile.cheese321.data.photobooth.entity.PhotoBooth
import com.hsmile.cheese321.data.photobooth.entity.QPhotoBooth.photoBooth
import com.hsmile.cheese321.data.photobooth.repository.dto.PhotoBoothSearchCriteria
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class PhotoBoothQueryRepository(
    private val queryFactory: JPAQueryFactory
) {

    fun findNearby(lat: Double, lng: Double, radius: Int): List<PhotoBooth> {
        return queryFactory
            .selectFrom(photoBooth)
            .where(
                withinDistance(lat, lng, radius)
            )
            .orderBy(
                distanceExpression(lat, lng).asc()
            )
            .fetch()
    }

    fun search(criteria: PhotoBoothSearchCriteria): List<PhotoBooth> {
        return queryFactory
            .selectFrom(photoBooth)
            .where(
                withinDistance(criteria.lat, criteria.lng, criteria.radius),
                regionFilter(criteria.region),
                brandFilter(criteria.brand)
            )
            .orderBy(
                distanceExpression(criteria.lat, criteria.lng).asc()
            )
            .fetch()
    }

    private fun withinDistance(lat: Double, lng: Double, radius: Int?): BooleanExpression? {
        return if (radius != null) {
            Expressions.booleanTemplate(
                "ST_DWithin({0}, ST_SetSRID(ST_MakePoint({1}, {2}), 4326), {3})",
                photoBooth.location, lng, lat, radius
            )
        } else null
    }

    private fun distanceExpression(lat: Double, lng: Double) =
        Expressions.numberTemplate(
            Double::class.java,
            "ST_Distance({0}, ST_SetSRID(ST_MakePoint({1}, {2}), 4326))",
            photoBooth.location, lng, lat
        )

    private fun regionFilter(region: String?): BooleanExpression? {
        return region?.let { photoBooth.region.eq(it) }
    }

    private fun brandFilter(brand: String?): BooleanExpression? {
        return brand?.let { photoBooth.brand.eq(it) }
    }
}
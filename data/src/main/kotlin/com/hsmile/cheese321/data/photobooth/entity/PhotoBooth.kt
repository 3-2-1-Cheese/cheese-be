package com.hsmile.cheese321.data.photobooth.entity

import jakarta.persistence.*
import org.locationtech.jts.geom.Point
import java.math.BigDecimal

@Entity
@Table(name = "photobooths")
class PhotoBooth(
    @Id
    @Column(length = 36)
    val id: String,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(nullable = false, length = 50)
    val brand: String,

    @Column(nullable = false, length = 50)
    val region: String,

    @Column(nullable = false, length = 200)
    val address: String,

    @Column(nullable = false, columnDefinition = "geography(POINT, 4326)")
    val location: Point,

    @Column(name = "review_count", nullable = false)
    val reviewCount: Int = 0,

    @Column(name = "image_urls", columnDefinition = "jsonb")
    val imageUrls: String? = null,

    @Column(name = "analysis_data", columnDefinition = "jsonb")
    val analysisData: String? = null
) {
    // JPA를 위한 기본 생성자
    protected constructor() : this(
        id = "",
        name = "",
        brand = "",
        region = "",
        address = "",
        location = org.locationtech.jts.geom.GeometryFactory().createPoint(org.locationtech.jts.geom.Coordinate(0.0, 0.0))
    )
}
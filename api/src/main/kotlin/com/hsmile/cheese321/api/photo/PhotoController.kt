package com.hsmile.cheese321.api.photo

import com.hsmile.cheese321.api.photo.dto.*
import org.springframework.web.bind.annotation.RestController

@RestController
class PhotoController : PhotoApi {

    override fun savePhotos(request: PhotoSaveRequest): PhotoSaveResponse {
        // TODO: 실제 구현
        // - QR scanId로 임시 데이터 조회
        // - 저장 옵션에 따라 처리 (전체/개별/앨범)
        // - 이미지 크롭 및 최종 저장
        // - 메타데이터 저장
        throw NotImplementedError("사진 저장 - 아직 구현 안됨")
    }

    override fun getMyPhotos(userId: String, page: Int?, size: Int?, sortBy: String?): PhotoListResponse {
        // TODO: 실제 구현
        // - 사용자별 사진 목록 조회
        // - 페이징 처리
        // - 정렬 (최신순, 이름순 등)
        throw NotImplementedError("내 사진 목록 - 아직 구현 안됨")
    }

    override fun getPhotoDetail(photoId: String): PhotoDetailResponse {
        // TODO: 실제 구현
        // - 사진 상세 정보 조회
        // - 메타데이터 포함
        throw NotImplementedError("사진 상세 조회 - 아직 구현 안됨")
    }

    override fun createAlbum(request: AlbumCreateRequest): AlbumResponse {
        // TODO: 실제 구현
        // - 앨범 생성
        // - 초기 사진들 추가 (선택사항)
        throw NotImplementedError("앨범 생성 - 아직 구현 안됨")
    }

    override fun getMyAlbums(userId: String): List<AlbumResponse> {
        // TODO: 실제 구현
        // - 사용자별 앨범 목록 조회
        throw NotImplementedError("내 앨범 목록 - 아직 구현 안됨")
    }

    override fun getAlbumDetail(albumId: String): AlbumDetailResponse {
        // TODO: 실제 구현
        // - 앨범 상세 정보 + 포함된 사진들
        throw NotImplementedError("앨범 상세 조회 - 아직 구현 안됨")
    }

    override fun addPhotosToAlbum(albumId: String, request: AlbumAddPhotosRequest): AlbumDetailResponse {
        // TODO: 실제 구현
        // - 기존 앨범에 사진들 추가
        throw NotImplementedError("앨범에 사진 추가 - 아직 구현 안됨")
    }
}
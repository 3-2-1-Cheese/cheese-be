package com.hsmile.cheese321.api.common.file

import com.google.cloud.storage.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.multipart.MultipartFile
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 파일 저장 서비스 - GCS 연동 (현재 더미 모드)
 *
 * TODO: 인프라팀 GCS 버킷 생성 완료 후 실제 연동
 * - app.gcs.enabled: false -> true 변경
 * - 더미 관련 메서드들 제거 또는 @Deprecated 처리
 */
@Service
class FileStorageService(
    @Autowired(required = false) private val storage: Storage?,  // required = false 추가
    private val webClientBuilder: WebClient.Builder,
    @Value("\${app.gcs.bucket-name}")
    private val bucketName: String,
    @Value("\${app.gcs.enabled:false}")
    private val gcsEnabled: Boolean  // TODO: 인프라 준비되면 true로 변경
) {

    private val webClient = webClientBuilder.build()

    companion object {
        private val ALLOWED_IMAGE_TYPES = setOf(
            "image/jpeg", "image/jpg", "image/png", "image/heic", "image/heif"
        )
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
    }

    /**
     * MultipartFile을 GCS에 업로드
     * TODO: 현재 더미 모드, GCS 버킷 준비되면 실제 업로드로 전환
     */
    fun uploadPhoto(file: MultipartFile, userId: String, photoId: String): FileUploadResult {
        // TODO: 인프라팀 작업 완료 후 이 if문 제거
        if (!gcsEnabled || storage == null) {
            println("GCS 비활성화 상태, 더미 응답 반환")
            return createDummyResult(photoId, file)
        }

        validateImageFile(file)

        val filePath = generatePhotoPath(userId, photoId, file.originalFilename)

        return try {
            val blobInfo = createBlobInfo(filePath, file, userId, photoId)
            val blob = storage.create(blobInfo, file.bytes)

            FileUploadResult(
                success = true,
                filePath = filePath,
                fileSize = file.size,
                contentType = file.contentType ?: "image/jpeg",
                gsUrl = "gs://$bucketName/$filePath",
                publicUrl = generatePublicUrl(filePath)
            )
        } catch (e: Exception) {
            FileUploadResult(
                success = false,
                error = "파일 업로드 실패: ${e.message}"
            )
        }
    }

    /**
     * URL에서 이미지를 다운로드하여 GCS에 업로드
     * TODO: 현재 더미 모드, QR 사진 저장 시 실제 GCS 업로드로 전환 필요
     */
    fun downloadAndUpload(imageUrl: String, userId: String, photoId: String, originalFilename: String): FileUploadResult {
        // TODO: 인프라팀 작업 완료 후 이 if문 제거
        if (!gcsEnabled || storage == null) {
            println("QR 사진 저장 - 더미 응답 반환")
            return createDummyResult(photoId, originalFilename)
        }

        return try {
            val imageBytes = downloadImageFromUrl(imageUrl)
            val contentType = detectContentType(imageBytes, originalFilename)

            if (imageBytes.size > MAX_FILE_SIZE) {
                return FileUploadResult(
                    success = false,
                    error = "다운로드한 파일이 너무 큽니다: ${imageBytes.size} bytes"
                )
            }

            val filePath = generateQrPhotoPath(userId, photoId, originalFilename)
            val blobInfo = createBlobInfoForBytes(filePath, imageBytes, contentType, userId, photoId)
            val blob = storage.create(blobInfo, imageBytes)

            FileUploadResult(
                success = true,
                filePath = filePath,
                fileSize = imageBytes.size.toLong(),
                contentType = contentType,
                gsUrl = "gs://$bucketName/$filePath",
                publicUrl = generatePublicUrl(filePath)
            )
        } catch (e: Exception) {
            FileUploadResult(
                success = false,
                error = "URL에서 파일 다운로드 및 업로드 실패: ${e.message}"
            )
        }
    }

    /**
     * Pre-signed URL 생성
     */
    fun generateDownloadUrl(filePath: String, userId: String): String {
        if (!gcsEnabled || storage == null) {
            return "https://example.com/dummy-download-url"
        }

        val blobId = BlobId.of(bucketName, filePath)
        val blob = storage.get(blobId) ?: throw FileNotFoundException("파일을 찾을 수 없습니다: $filePath")

        val fileUserId = blob.metadata?.get("userId")
        if (fileUserId != userId) {
            throw FileAccessDeniedException("파일 접근 권한이 없습니다")
        }

        return blob.signUrl(1, TimeUnit.HOURS).toString()
    }

    /**
     * 파일 삭제
     */
    fun deleteFile(filePath: String, userId: String): Boolean {
        if (!gcsEnabled || storage == null) {
            return true  // 더미에서는 항상 성공
        }

        return try {
            val blobId = BlobId.of(bucketName, filePath)
            val blob = storage.get(blobId) ?: return false

            val fileUserId = blob.metadata?.get("userId")
            if (fileUserId != userId) {
                throw FileAccessDeniedException("파일 삭제 권한이 없습니다")
            }

            storage.delete(blobId)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 여러 파일 일괄 삭제
     */
    fun deleteFiles(filePaths: List<String>, userId: String): BatchDeleteResult {
        if (!gcsEnabled || storage == null) {
            return BatchDeleteResult(
                deletedCount = filePaths.size,
                failedCount = 0,
                deletedFiles = filePaths,
                failedFiles = emptyList()
            )
        }

        if (filePaths.isEmpty()) {
            return BatchDeleteResult(0, 0, emptyList(), emptyList())
        }

        val blobIds = filePaths.map { BlobId.of(bucketName, it) }

        return try {
            val blobs = storage.get(blobIds)
            val validBlobIds = mutableListOf<BlobId>()
            val invalidFiles = mutableListOf<String>()

            blobs.forEachIndexed { index, blob ->
                val filePath = filePaths[index]
                if (blob != null && blob.metadata?.get("userId") == userId) {
                    validBlobIds.add(blob.blobId)
                } else {
                    invalidFiles.add(filePath)
                }
            }

            val deleteResults = if (validBlobIds.isNotEmpty()) {
                storage.delete(validBlobIds)
            } else {
                emptyList()
            }

            val deletedFiles = mutableListOf<String>()
            val failedFiles = mutableListOf<String>()

            validBlobIds.forEachIndexed { index, blobId ->
                if (index < deleteResults.size && deleteResults[index]) {
                    deletedFiles.add(blobId.name)
                } else {
                    failedFiles.add(blobId.name)
                }
            }

            failedFiles.addAll(invalidFiles)

            BatchDeleteResult(
                deletedCount = deletedFiles.size,
                failedCount = failedFiles.size,
                deletedFiles = deletedFiles,
                failedFiles = failedFiles
            )
        } catch (e: Exception) {
            BatchDeleteResult(
                deletedCount = 0,
                failedCount = filePaths.size,
                deletedFiles = emptyList(),
                failedFiles = filePaths
            )
        }
    }

    // ===== TODO: 인프라 준비되면 제거할 더미 메서드들 =====

    /**
     * @deprecated 더미 모드용, GCS 연결 후 제거 예정
     */
    private fun createDummyResult(photoId: String, file: MultipartFile): FileUploadResult {
        val dummyPath = "dummy/photos/${photoId}_${file.originalFilename}"
        println("가짜 파일 업로드: ${file.originalFilename}")
        return FileUploadResult(
            success = true,
            filePath = dummyPath,
            fileSize = file.size,
            contentType = file.contentType ?: "image/jpeg",
            gsUrl = "gs://dummy-bucket/$dummyPath",
            publicUrl = "https://example.com/dummy-photo.jpg"
        )
    }

    /**
     * @deprecated 더미 모드용, GCS 연결 후 제거 예정
     */
    private fun createDummyResult(photoId: String, originalFilename: String): FileUploadResult {
        val dummyPath = "dummy/qr-photos/${photoId}_${originalFilename}"
        println("🔥 [DUMMY] 가짜 QR 사진 저장: $originalFilename")
        return FileUploadResult(
            success = true,
            filePath = dummyPath,
            fileSize = 1024L,
            contentType = "image/jpeg",
            gsUrl = "gs://dummy-bucket/$dummyPath",
            publicUrl = "https://example.com/dummy-qr-photo.jpg"
        )
    }

    // ===== Private 메서드들 =====

    private fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("파일이 비어있습니다")
        }

        if (file.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다")
        }

        val contentType = file.contentType
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.lowercase())) {
            throw IllegalArgumentException("지원하지 않는 파일 형식입니다. (지원: JPG, PNG, HEIC)")
        }
    }

    private fun generatePhotoPath(userId: String, photoId: String, originalFilename: String?): String {
        val datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val extension = getFileExtension(originalFilename ?: "unknown.jpg")
        return "photos/$userId/$datePrefix/${photoId}$extension"
    }

    private fun generateQrPhotoPath(userId: String, photoId: String, originalFilename: String): String {
        val datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val extension = getFileExtension(originalFilename)
        return "photos/$userId/qr-imports/$datePrefix/${photoId}$extension"
    }

    private fun getFileExtension(filename: String): String {
        val lastDotIndex = filename.lastIndexOf('.')
        return if (lastDotIndex > 0) filename.substring(lastDotIndex) else ".jpg"
    }

    private fun generatePublicUrl(filePath: String): String {
        return "https://storage.googleapis.com/$bucketName/$filePath"
    }

    private fun createBlobInfo(filePath: String, file: MultipartFile, userId: String, photoId: String): BlobInfo {
        return BlobInfo.newBuilder(bucketName, filePath)
            .setContentType(file.contentType)
            .setMetadata(mapOf(
                "userId" to userId,
                "photoId" to photoId,
                "originalFilename" to (file.originalFilename ?: "unknown"),
                "uploadedAt" to LocalDateTime.now().toString(),
                "source" to "direct-upload"
            ))
            .build()
    }

    private fun createBlobInfoForBytes(filePath: String, imageBytes: ByteArray, contentType: String, userId: String, photoId: String): BlobInfo {
        return BlobInfo.newBuilder(bucketName, filePath)
            .setContentType(contentType)
            .setMetadata(mapOf(
                "userId" to userId,
                "photoId" to photoId,
                "uploadedAt" to LocalDateTime.now().toString(),
                "source" to "qr-import"
            ))
            .build()
    }

    private fun downloadImageFromUrl(imageUrl: String): ByteArray {
        return try {
            webClient.get()
                .uri(imageUrl)
                .retrieve()
                .bodyToMono(ByteArray::class.java)
                .timeout(Duration.ofSeconds(30))
                .block() ?: throw RuntimeException("이미지 다운로드 결과가 null입니다")
        } catch (e: Exception) {
            throw RuntimeException("이미지 다운로드 실패: $imageUrl", e)
        }
    }

    private fun detectContentType(imageBytes: ByteArray, filename: String): String {
        return when {
            imageBytes.size >= 2 && imageBytes[0] == 0xFF.toByte() && imageBytes[1] == 0xD8.toByte() -> "image/jpeg"
            imageBytes.size >= 8 && imageBytes.sliceArray(1..3).contentEquals("PNG".toByteArray()) -> "image/png"
            filename.lowercase().endsWith(".heic") -> "image/heic"
            filename.lowercase().endsWith(".heif") -> "image/heif"
            else -> "image/jpeg"
        }
    }
}

// ===== 데이터 클래스들 =====

data class FileUploadResult(
    val success: Boolean,
    val filePath: String = "",
    val fileSize: Long = 0,
    val contentType: String = "",
    val gsUrl: String = "",
    val publicUrl: String = "",
    val error: String? = null
)

data class BatchDeleteResult(
    val deletedCount: Int,
    val failedCount: Int,
    val deletedFiles: List<String>,
    val failedFiles: List<String>
)

// ===== 예외 클래스들 =====

class FileNotFoundException(message: String) : RuntimeException(message)
class FileAccessDeniedException(message: String) : RuntimeException(message)
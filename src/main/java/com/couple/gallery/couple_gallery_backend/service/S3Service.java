package com.couple.gallery.couple_gallery_backend.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.couple.gallery.couple_gallery_backend.dto.S3KeySet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.coobird.thumbnailator.Thumbnails;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName; // application.yml에서 주입받은 S3 버킷 이름

    /**
     * S3에 파일 업로드
     * @param multipartFile 업로드할 파일
     * @param coupleId 해당 파일이 속한 커플 ID (S3 경로 생성용)
     * @return S3에 저장된 파일의 키 (경로 + 파일명)
     */

    @Value("${cloud.aws.s3.public-url}")
    private String publicUrl;

    public S3KeySet uploadFileAndThumbnail(MultipartFile multipartFile, Long coupleId) throws IOException {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

//        // 1. 원본 Key 생성
//        String originalFilename = multipartFile.getOriginalFilename();
//        String fileExtension = "";
//        if (originalFilename != null && originalFilename.contains(".")) {
//            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
//        }
//        String originalKey = "photos/" + coupleId + "/" + UUID.randomUUID() + fileExtension;
//
//        // 2. 원본 업로드
//        ObjectMetadata originalMeta = new ObjectMetadata();
//        originalMeta.setContentLength(multipartFile.getSize());
//        originalMeta.setContentType(multipartFile.getContentType());
//        amazonS3Client.putObject(new PutObjectRequest(bucketName, originalKey, multipartFile.getInputStream(), originalMeta)
//                .withCannedAcl(CannedAccessControlList.Private));
//
//        // 3. 썸네일 생성
//        BufferedImage thumbnailImage = Thumbnails.of(multipartFile.getInputStream())
//                .size(300, 300) // 원하는 크기
//                .outputFormat("jpg")
//                .asBufferedImage();
//
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        ImageIO.write(thumbnailImage, "jpg", os);
//        byte[] thumbnailBytes = os.toByteArray();
//
//        // 4. 썸네일 Key 생성
//        String thumbnailKey = "photos/" + coupleId + "/thumbnails/" + UUID.randomUUID() + "_thumb.jpg";
//
//        ObjectMetadata thumbMeta = new ObjectMetadata();
//        thumbMeta.setContentLength(thumbnailBytes.length);
//        thumbMeta.setContentType("image/jpeg");
//
//        amazonS3Client.putObject(new PutObjectRequest(bucketName, thumbnailKey, new ByteArrayInputStream(thumbnailBytes), thumbMeta)
//                .withCannedAcl(CannedAccessControlList.Private));
//
//        // 5. KeySet 반환
//        return new S3KeySet(originalKey, thumbnailKey);
//    }
        // 1. 사용자가 구분하기 힘든 수준으로 압축 (1200px, 품질 85%)
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Thumbnails.of(multipartFile.getInputStream())
                .size(1200, 1200)       // 긴 쪽 기준 1200px로 리사이징
                .keepAspectRatio(true)  // 비율 유지
                .outputFormat("jpg")
                .outputQuality(0.85)    // 85% 품질 (용량 대비 화질 밸런스)
                .toOutputStream(os);
        byte[] compressedBytes = os.toByteArray();

        // 2. Key 생성 (썸네일 Key 없이 단일 Key만)
        String originalKey = "photos/" + coupleId + "/" + UUID.randomUUID() + ".jpg";

        // 3. 업로드
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(compressedBytes.length);
        meta.setContentType("image/jpeg");
        amazonS3Client.putObject(new PutObjectRequest(
                bucketName,
                originalKey,
                new ByteArrayInputStream(compressedBytes),
                meta)
                .withCannedAcl(CannedAccessControlList.Private));

        // 4. thumbnailKey 없이 originalKey만 반환 (S3KeySet 구조 유지)
        return new S3KeySet(originalKey, null);
    }
    /**
     * S3에 프로필사진 올리기
     */
    // S3Service.java (프로필 이미지 업로드 전용 메소드)

//    public String uploadProfileImage(MultipartFile multipartFile, Long userId) throws IOException {
//        if (multipartFile.isEmpty()) {
//            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
//        }
//
//        // 1. 썸네일(프로필 사진 크기) 생성
//        BufferedImage profileImage = Thumbnails.of(multipartFile.getInputStream())
//                .size(300, 300) // 300x300 크기로 리사이징
//                .outputFormat("jpg")
//                .asBufferedImage();
//
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        // 리사이징된 이미지를 바이트 배열로 변환
//        ImageIO.write(profileImage, "jpg", os);
//        byte[] profileBytes = os.toByteArray();
//
//        // 2. S3 Key 생성
//        // 경로를 'profiles/{userId}/...' 형태로 깔끔하게 변경합니다.
//        String profileKey = "profiles/" + userId + "/" + UUID.randomUUID() + ".jpg";
//
//        // 3. 메타데이터 설정
//        ObjectMetadata profileMeta = new ObjectMetadata();
//        profileMeta.setContentLength(profileBytes.length);
//        profileMeta.setContentType("image/jpeg"); // 리사이징 과정에서 형식 고정
//
//        // 4. S3에 업로드
//        amazonS3Client.putObject(new PutObjectRequest(
//                bucketName,
//                profileKey,
//                new ByteArrayInputStream(profileBytes), // 바이트 배열을 InputStream으로 다시 변환
//                profileMeta)
//                .withCannedAcl(CannedAccessControlList.Private)); // 접근 권한 설정
//
//        // 5. Key 반환
//        return profileKey;
    public String uploadProfileImage(MultipartFile multipartFile, Long userId) throws IOException {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 1. 프로필용 압축 (300x300, 품질 85%)
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Thumbnails.of(multipartFile.getInputStream())
                .size(300, 300)
                .keepAspectRatio(false) // 프로필은 정방형 강제
                .outputFormat("jpg")
                .outputQuality(0.85)
                .toOutputStream(os);
        byte[] profileBytes = os.toByteArray();

        // 2. Key 생성
        String profileKey = "photos/" + userId + "/profile/" + UUID.randomUUID() + "_profile_" + userId + "_" + System.currentTimeMillis() + ".jpg";

        // 3. 업로드
        ObjectMetadata profileMeta = new ObjectMetadata();
        profileMeta.setContentLength(profileBytes.length);
        profileMeta.setContentType("image/jpeg");
        amazonS3Client.putObject(new PutObjectRequest(
                bucketName,
                profileKey,
                new ByteArrayInputStream(profileBytes),
                profileMeta)
                .withCannedAcl(CannedAccessControlList.Private));

        // 4. 프라이빗 Key 대신 퍼블릭 URL로 반환
        return publicUrl + "/" + profileKey;
    }


    /**
     * S3에 저장된 프라이빗 파일에 대한 Pre-signed URL 생성
     * @param s3Key S3에 저장된 파일의 키 (예: photos/1/uuid.jpg)
     * @return Pre-signed URL 문자열
     */
    public String generatePresignedUrl(String s3Key) {
        // URL 유효 시간 (예: 1시간)
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60; // 1시간
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucketName, s3Key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        URL url = amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    /**
     * S3에서 파일 삭제
     * @param s3Key 삭제할 파일의 S3 키
     */
    public void deleteFile(String s3Key) {
        try {
            amazonS3Client.deleteObject(bucketName, s3Key);
            log.info("S3 파일 삭제 성공: {}", s3Key);
        } catch (Exception e) {
            log.error("S3 파일 삭제 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("S3 파일 삭제 실패", e);
        }
    }
}
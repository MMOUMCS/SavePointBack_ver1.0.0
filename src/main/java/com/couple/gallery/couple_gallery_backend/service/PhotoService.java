package com.couple.gallery.couple_gallery_backend.service;

import com.couple.gallery.couple_gallery_backend.dto.PhotoResponse;
import com.couple.gallery.couple_gallery_backend.domain.Photo;
import com.couple.gallery.couple_gallery_backend.dto.S3KeySet;
import com.couple.gallery.couple_gallery_backend.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final S3Service s3Service;

    /**
     * 사진을 S3에 업로드하고 DB에 정보 저장
     * @param file 업로드할 이미지 파일
     * @param coupleId 현재 사용자의 커플 ID (인증/인가 미들웨어에서 가져옴)
     * @param uploaderId 업로드하는 사용자 ID (인증/인가 미들웨어에서 가져옴)
     * @param caption 사진 설명 (옵셔널)
     * @return 저장된 사진 정보 DTO
     */
    @Transactional
    public PhotoResponse uploadPhoto(MultipartFile file, Long coupleId, Long uploaderId, String caption) throws IOException {

        // 1. S3에 원본과 썸네일 모두 업로드하고 Key Set 받기
        // S3KeySet은 originalKey(원본 키)와 thumbnailKey(썸네일 키)를 가집니다.
        S3KeySet keySet = s3Service.uploadFileAndThumbnail(file, coupleId);

        // 2. DB에 사진 정보 저장
        Photo photo = Photo.builder()
                .coupleId(coupleId)
                .uploaderId(uploaderId)
                .s3Key(keySet.getOriginalKey())       // 원본 이미지 S3 Key
                .thumbnailS3Key(keySet.getThumbnailKey()) // 썸네일 이미지 S3 Key
                .caption(caption)
                .build();

        Photo savedPhoto = photoRepository.save(photo);

        // 3. Pre-signed URL 생성하여 DTO로 반환
        String originalUrl = s3Service.generatePresignedUrl(keySet.getOriginalKey());
        String thumbnailUrl = s3Service.generatePresignedUrl(keySet.getThumbnailKey());

        return PhotoResponse.from(savedPhoto, originalUrl, thumbnailUrl);
    }


    /**
     * 특정 커플의 모든 사진 목록 조회
     * @param coupleId 조회할 커플 ID
     * @return 사진 목록 DTO
     */
    @Transactional(readOnly = true)
    public List<PhotoResponse> getPhotosByCoupleId(Long coupleId) {
        List<Photo> photos = photoRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId);

        return photos.stream()
                .map(photo -> {
                    String imageUrl = s3Service.generatePresignedUrl(photo.getS3Key());
                    String thumbnailUrl = s3Service.generatePresignedUrl(photo.getThumbnailS3Key());
                    return PhotoResponse.from(photo, imageUrl, thumbnailUrl);
                })
                .collect(Collectors.toList());
    }


    /**
     * 사진 삭제
     * @param photoId 삭제할 사진 ID
     * @param coupleId 요청한 사용자의 커플 ID
     * @param uploaderId 요청한 사용자 ID (삭제 권한 확인용)
     */
    @Transactional
    public void deletePhoto(Long photoId, Long coupleId, Long uploaderId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("사진을 찾을 수 없습니다. ID: " + photoId));

        // 1. 해당 사진이 요청한 커플의 사진인지 확인 (필수)
        if (!photo.getCoupleId().equals(coupleId)) {
            throw new IllegalArgumentException("해당 커플의 사진이 아닙니다. 삭제 권한이 없습니다.");
        }

        // 2. 사진을 올린 본인(uploaderId)만 삭제할 수 있도록 확인
        if (!photo.getUploaderId().equals(uploaderId)) {
            // 요청한 사용자 ID와 사진을 업로드한 사용자 ID가 다르면 삭제 불가
            throw new IllegalArgumentException("사진을 올린 본인만 삭제할 수 있습니다.");
        }

        // 3. S3에서 파일 삭제
        s3Service.deleteFile(photo.getS3Key());
        s3Service.deleteFile(photo.getThumbnailS3Key());

        // 4. DB에서 사진 정보 삭제
        photoRepository.delete(photo);
    }
}
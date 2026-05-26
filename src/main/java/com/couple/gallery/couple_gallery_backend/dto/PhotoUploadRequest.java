package com.couple.gallery.couple_gallery_backend.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Setter
public class PhotoUploadRequest {
    private MultipartFile file;
    private String caption;
}
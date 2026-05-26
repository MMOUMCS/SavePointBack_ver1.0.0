package com.couple.gallery.couple_gallery_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3KeySet {
    private final String originalKey;
    private final String thumbnailKey;
}

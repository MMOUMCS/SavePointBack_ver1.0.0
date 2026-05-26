package com.couple.gallery.couple_gallery_backend.dto;

import lombok.Builder;
import lombok.Getter;

// dto/PartnerResponse.java
@Getter
@Builder
public class PartnerResponse {

    private Long id;
    private String name;
    private String profileImageUrl;
    private String email;
}
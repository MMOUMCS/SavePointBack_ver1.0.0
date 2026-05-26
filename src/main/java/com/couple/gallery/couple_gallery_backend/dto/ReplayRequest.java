package com.couple.gallery.couple_gallery_backend.dto;

import lombok.Getter;

@Getter
public class ReplayRequest {
    private String youtubeVideoId;
    private String title;
    private String uploaderName;
}
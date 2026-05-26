package com.couple.gallery.couple_gallery_backend.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class EventRequest {
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    private String description;
    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalDateTime startDateTime;
    @NotNull(message = "종료 시간은 필수입니다.")
    private LocalDateTime endDateTime;
    private boolean isAllDay;
    private String location;
}
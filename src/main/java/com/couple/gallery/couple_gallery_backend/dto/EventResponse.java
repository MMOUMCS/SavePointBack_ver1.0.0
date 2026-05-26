package com.couple.gallery.couple_gallery_backend.dto;

import com.couple.gallery.couple_gallery_backend.domain.Event;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class EventResponse {
    private Long id;
    private Long coupleId;
    private Long userId;
    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean isAllDay;
    private String location;
    private LocalDateTime createdAt;

    public static EventResponse from(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .coupleId(event.getCoupleId())
                .userId(event.getUserId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDateTime(event.getStartDateTime())
                .endDateTime(event.getEndDateTime())
                .isAllDay(event.isAllDay())
                .location(event.getLocation())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
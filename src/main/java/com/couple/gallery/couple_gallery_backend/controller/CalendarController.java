package com.couple.gallery.couple_gallery_backend.controller;

import com.couple.gallery.couple_gallery_backend.config.security.UserDetailsImpl;
import com.couple.gallery.couple_gallery_backend.dto.EventRequest;
import com.couple.gallery.couple_gallery_backend.dto.EventResponse;
import com.couple.gallery.couple_gallery_backend.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List; // List import 추가

@Tag(name = "커플 캘린더 API", description = "일정 생성, 조회, 수정, 삭제 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class CalendarController {

    private final CalendarService calendarService;

    // 1. 일정 생성 (POST)
    @Operation(
            summary = "커플 일정 등록 (POST)",
            description = "로그인한 사용자의 커플 캘린더에 새로운 일정을 생성합니다. 요청 바디(Body)에 정보 담기 필수."
    )
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @RequestBody @Valid EventRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        System.out.println("DEBUG: principal is " + principal);
        //  principal에서 coupleId와 userId를 직접 추출
        // Long coupleId = principal.getCoupleId();
        // Long userId = principal.getUserId();

        Long coupleId = principal.getCoupleId();
        Long userId = principal.getUserId();

        EventResponse response = calendarService.createEvent(coupleId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. 일정 수정 (PUT) - 커플 공동 수정 가능
    @Operation(
            summary = "일정 수정 (PUT)",
            description = "지정한 일정 ID(eventId)의 내용을 수정합니다. 커플 중 누구나 공동으로 수정할 수 있습니다."
    )
    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @RequestBody @Valid EventRequest request,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        // principal에서 coupleId를 직접 추출
        Long coupleId = principal.getCoupleId();

        EventResponse response = calendarService.updateEvent(eventId, coupleId, request);
        return ResponseEntity.ok(response);
    }

    // 3. 기간별 일정 조회 (GET)
    @Operation(
            summary = "기간별 일정 조회️ (GET)",
            description = "시작일(start)과 종료일(end) 사이의 모든 커플 일정을 리스트로 가져옵니다. (날짜 형식 예시: 2026-05-21T00:00:00)"
    )
    @GetMapping
    public ResponseEntity<List<EventResponse>> getEventsInPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        // principal에서 coupleId를 직접 추출
        Long coupleId = principal.getCoupleId();
        List<EventResponse> responses = calendarService.getEventsByPeriod(coupleId, start, end);
        return ResponseEntity.ok(responses);
    }

    // 4. 일정 삭제 (DELETE) - 커플 공동 삭제 가능
    @Operation(
            summary = "일정 삭제 (DELETE)",
            description = "지정한 일정 ID(eventId)를 삭제합니다. 커플 중 누구나 공동으로 삭제할 수 있으며, 삭제 후 복구되지 않습니다."
    )
    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetailsImpl principal) {

        // principal에서 coupleId를 직접 추출
        Long coupleId = principal.getCoupleId();
        calendarService.deleteEvent(eventId, coupleId);

        return ResponseEntity.noContent().build();
    }
}
// CalendarService.java
package com.couple.gallery.couple_gallery_backend.service;

import com.couple.gallery.couple_gallery_backend.domain.Event;
import com.couple.gallery.couple_gallery_backend.dto.EventRequest;
import com.couple.gallery.couple_gallery_backend.dto.EventResponse;
import com.couple.gallery.couple_gallery_backend.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final EventRepository eventRepository;

    /**
     * 1. 일정 생성
     */
    @Transactional
    public EventResponse createEvent(Long coupleId, Long userId, EventRequest request) { //  Long coupleId, Long userId 받음
        Event event = Event.builder()
                .coupleId(coupleId) // Controller에서 받은 ID 사용
                .userId(userId)     // Controller에서 받은 ID 사용
                .title(request.getTitle())
                .description(request.getDescription())
                .startDateTime(request.getStartDateTime())
                .endDateTime(request.getEndDateTime())
                .isAllDay(request.isAllDay())
                .location(request.getLocation())
                .build();

        Event savedEvent = eventRepository.save(event);
        return EventResponse.from(savedEvent);
    }

    /**
     * 2. 일정 수정 (커플 공동 수정 가능)
     */
    @Transactional
    public EventResponse updateEvent(Long eventId, Long coupleId, EventRequest request) { //  Long coupleId 받음
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 일정을 찾을 수 없습니다."));

        //  권한 검사: Controller에서 받은 coupleId와 일정의 coupleId 일치 확인
        if (!event.getCoupleId().equals(coupleId)) {
            throw new IllegalArgumentException("해당 일정에 대한 수정 권한이 없습니다. (커플 불일치)");
        }

        event.update(request.getTitle(), request.getDescription(),
                request.getStartDateTime(), request.getEndDateTime(),
                request.isAllDay(), request.getLocation());

        return EventResponse.from(event);
    }

    /**
     * 3. 기간별 일정 조회
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByPeriod(Long coupleId, LocalDateTime start, LocalDateTime end) { //  Long coupleId 받음
        List<Event> events = eventRepository
                .findByCoupleIdAndStartDateTimeBetweenOrderByStartDateTimeAsc(coupleId, start, end);

        return events.stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 4. 일정 삭제
     */
    @Transactional
    public void deleteEvent(Long eventId, Long coupleId) { //  Long coupleId 받음
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));

        //  권한 검사: Controller에서 받은 coupleId와 일정의 coupleId 일치 확인
        if (!event.getCoupleId().equals(coupleId)) {
            throw new IllegalArgumentException("해당 일정에 대한 삭제 권한이 없습니다. (커플 불일치)");
        }

        eventRepository.delete(event);
    }
}
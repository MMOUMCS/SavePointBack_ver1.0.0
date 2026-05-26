package com.couple.gallery.couple_gallery_backend.repository;

import com.couple.gallery.couple_gallery_backend.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 특정 커플 ID와 기간 내의 일정을 시작 시간 오름차순으로 조회
    List<Event> findByCoupleIdAndStartDateTimeBetweenOrderByStartDateTimeAsc(
            Long coupleId, LocalDateTime start, LocalDateTime end);
}
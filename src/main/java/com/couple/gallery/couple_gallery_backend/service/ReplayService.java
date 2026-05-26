package com.couple.gallery.couple_gallery_backend.service;

import com.couple.gallery.couple_gallery_backend.domain.Replay;
import com.couple.gallery.couple_gallery_backend.dto.ReplayRequest;
import com.couple.gallery.couple_gallery_backend.dto.ReplayResponse;
import com.couple.gallery.couple_gallery_backend.repository.ReplayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplayService {

    private final ReplayRepository replayRepository;

    // 영상 저장
    public ReplayResponse save(ReplayRequest request, Long coupleId) {
        Replay replay = new Replay(
                request.getYoutubeVideoId(),
                request.getTitle(),
                request.getUploaderName(),
                coupleId
        );
        return ReplayResponse.from(replayRepository.save(replay));
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public List<ReplayResponse> findAll(Long coupleId) {
        return replayRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId)
                .stream()
                .map(ReplayResponse::from)
                .collect(Collectors.toList());
    }

    // 단건 조회 + 조회수 증가
    public ReplayResponse findById(Long id, Long coupleId) {
        Replay replay = replayRepository.findByIdAndCoupleId(id, coupleId)
                .orElseThrow(() -> new IllegalArgumentException("영상을 찾을 수 없습니다."));
        replay.increaseViewCount();
        return ReplayResponse.from(replay);
    }

    // 좋아요
    public ReplayResponse like(Long id, Long coupleId) {
        Replay replay = replayRepository.findByIdAndCoupleId(id, coupleId)
                .orElseThrow(() -> new IllegalArgumentException("영상을 찾을 수 없습니다."));
        replay.increaseLikeCount();
        return ReplayResponse.from(replay);
    }

    // 삭제
    public void delete(Long id, Long coupleId) {
        Replay replay = replayRepository.findByIdAndCoupleId(id, coupleId)
                .orElseThrow(() -> new IllegalArgumentException("영상을 찾을 수 없습니다."));
        replayRepository.delete(replay);
    }
}
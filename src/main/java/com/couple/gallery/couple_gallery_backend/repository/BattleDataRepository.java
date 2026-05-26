package com.couple.gallery.couple_gallery_backend.repository;

import com.couple.gallery.couple_gallery_backend.domain.BattleData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BattleDataRepository extends JpaRepository<BattleData, Long> {
    List<BattleData> findByUserIdOrderByPlayDateDesc(String userId);
    List<BattleData> findByUserIdAndGameNameOrderByPlayDateDesc(String userId, BattleData.GameType gameName);
}


package com.couple.gallery.couple_gallery_backend.service;

import com.couple.gallery.couple_gallery_backend.domain.BattleData;
import com.couple.gallery.couple_gallery_backend.dto.BattleDataRequest;
import com.couple.gallery.couple_gallery_backend.repository.BattleDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BattleDataService {

    private final BattleDataRepository battleDataRepository;

    @Transactional
    public BattleData saveBattle(String userId, BattleDataRequest request) {
        BattleData battleData = new BattleData();
        battleData.setUserId(userId);
        battleData.setGameName(request.getGameName());
        battleData.setResult(request.getResult());
        battleData.setPlayDate(request.getPlayDate());
        battleData.setPlayTime(request.getPlayTime());
        battleData.setKills(request.getKills());
        battleData.setDeaths(request.getDeaths());
        battleData.setMemo(request.getMemo());
        return battleDataRepository.save(battleData);
    }

        /**
         *  배틀 전적 수정 로직 (몸통 메서드)
         */
        @Transactional
        public void updateBattle(Long id, BattleDataRequest request) {
            BattleData battleData = battleDataRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("해당 전적 기록이 존재하지 않습니다. id=" + id));

            battleData.setGameName(request.getGameName());
            battleData.setResult(request.getResult());
            battleData.setPlayDate(request.getPlayDate());
            battleData.setPlayTime(request.getPlayTime());
            battleData.setKills(request.getKills());
            battleData.setDeaths(request.getDeaths());
            battleData.setMemo(request.getMemo());
        }

    public List<BattleData> getBattles(String userId, BattleData.GameType gameType) {
        if (gameType == null) {
            return battleDataRepository.findByUserIdOrderByPlayDateDesc(userId);
        }
        return battleDataRepository.findByUserIdAndGameNameOrderByPlayDateDesc(userId, gameType);
    }


    @Transactional
    public void deleteBattle(Long id) {
        // 데이터가 존재하는지 먼저 확인하고, 있으면 한 방에 삭제
        if (!battleDataRepository.existsById(id)) {
            throw new IllegalArgumentException("해당 전적 기록이 존재하지 않습니다. id=" + id);
        }
        battleDataRepository.deleteById(id);
    }
}

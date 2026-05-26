package com.couple.gallery.couple_gallery_backend.controller;

import com.couple.gallery.couple_gallery_backend.domain.BattleData;
import com.couple.gallery.couple_gallery_backend.dto.BattleDataRequest;
import com.couple.gallery.couple_gallery_backend.service.BattleDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Tag(name = "배틀 데이터 API", description = "승/패/플레이 시간/날짜/게임 종류 등을 저장합니다.")
@RestController
@RequestMapping("/api/battle")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 혹시 몰라 넣어둔 CORS 방어막
public class BattleDataController {

    private final BattleDataService battleDataService;

    @Operation(
            summary = "배틀 데이터 저장 (POST)",
            description = "로그인한 사용자의 배틀 데이터에 새로운 데이터를 생성합니다. 요청 바디(Body)에 정보 담기 필수."
    )
    // 1. 배틀 데이터 저장
    @PostMapping
    public BattleData saveBattleData(@RequestBody BattleDataRequest request, Principal principal) {
        String userId = principal.getName(); // 시큐리티 인증 객체에서 유저 ID 추출
        return battleDataService.saveBattle(userId, request);
    }

    @Operation(
            summary = "배틀 데이터 조회 (GET)",
            description = "로그인한 사용자의 배틀 데이터를 불러옵니다."
    )
    // 2. 배틀 데이터 조회 및 필터링
    @GetMapping
    public List<BattleData> getBattleData(
            @RequestParam(required = false) String gameName,
            Principal principal) {
        String userId = principal.getName();

        if (gameName == null || "전체".equals(gameName)) {
            return battleDataService.getBattles(userId, null);
        }

        BattleData.GameType type = BattleData.GameType.valueOf(gameName); // 문자열 → enum 변환
        return battleDataService.getBattles(userId, type);
    }

    @Operation(
            summary = "배틀 데이터 수정 (PUT)",
            description = "로그인한 사용자의 배틀 데이터를 수정합니다."
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBattleData(
            @PathVariable Long id,
            @RequestBody BattleDataRequest request
    ) {
        // 기존 데이터를 id로 찾아서 request에 담긴 값으로 update 후 저장하는 서비스 로직 호출
        battleDataService.updateBattle(id, request);
        return ResponseEntity.ok().body("수정 완료");
    }

    @Operation(
            summary = "배틀 데이터 삭제 (DELETE)",
            description = "로그인한 사용자의 배틀 데이터를 삭제합니다."
    )
    @DeleteMapping("/{id}") //
    public ResponseEntity<?> deleteBattleData(@PathVariable Long id) {
        // 서비스단의 삭제 로직 호출
        battleDataService.deleteBattle(id);
        return ResponseEntity.ok().body("삭제 완료");
    }

}
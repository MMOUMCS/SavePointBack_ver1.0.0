package com.couple.gallery.couple_gallery_backend.dto;

import com.couple.gallery.couple_gallery_backend.domain.BattleData;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter @Setter
public class BattleDataRequest {
    private BattleData.GameType gameName;
    private String result;
    private LocalDate playDate;
    private Integer playTime;
    private Integer kills;
    private Integer deaths;
    private String memo;
}
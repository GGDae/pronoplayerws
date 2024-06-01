package com.pronoplayer.app.bo;

import com.pronoplayer.app.bo.lolesport.Team;

import lombok.Data;

@Data
public class MatchStat {
    private Team team;
    private Integer nbVotes;
    private Double votePercent;
}

package com.pronoplayer.app.bo;

import lombok.Data;

@Data
public class MatchScore {
    private String matchId;
    private String dateTime;
    private String winner;
    private String score;
    private boolean isBo;
}

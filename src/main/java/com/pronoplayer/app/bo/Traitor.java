package com.pronoplayer.app.bo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Traitor {
    private String userId;
    private String displayName;
    private Integer numberOfTreacheries;
    private Integer totalMatchs;

    public void addTreachery() {
        this.numberOfTreacheries += 1;
    }

    public void addMatch() {
        this.totalMatchs += 1;
    }

    public Traitor() {
        this.numberOfTreacheries = 0;
        this.totalMatchs = 0;
    }
}

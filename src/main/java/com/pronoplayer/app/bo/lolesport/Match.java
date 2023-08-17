package com.pronoplayer.app.bo.lolesport;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Match {
    public String id;
    private String dateTime;
    public boolean inProgress;
    public String result;
    public String score;
    public String state;
    public ArrayList<String> flags;
    public ArrayList<Team> teams;
    public Strategy strategy;
}

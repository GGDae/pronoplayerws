package com.pronoplayer.app.bo.lolesport;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
    public Date startTime;
    public String state;
    public String type;
    public String blockName;
    public League league;
    public Match match;
}

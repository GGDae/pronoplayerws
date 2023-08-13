package com.pronoplayer.app.bo.lolesport;

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule {
    public String league;
    public ArrayList<Event> events;
}

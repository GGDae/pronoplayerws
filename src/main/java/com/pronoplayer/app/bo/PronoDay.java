package com.pronoplayer.app.bo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pronoplayer.app.bo.lolesport.Match;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PronoDay {
    private String date;
    private Integer day;
    private List<Match> matchs;
}

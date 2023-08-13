package com.pronoplayer.app.bo.lolesport;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Section {
    public String name;
    public List<Column> columns;
    public List<Ranking> rankings;
    public List<Match> matches;
}

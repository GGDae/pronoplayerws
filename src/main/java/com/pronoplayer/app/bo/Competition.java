package com.pronoplayer.app.bo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document
public class Competition {
    @Id
    private String id;
    private String name;
    private String year;
    private String split;
    private String tournamentStartDate;
    private String leagueId;
    private String tournamentId;
    private boolean current;
    private String color;
}

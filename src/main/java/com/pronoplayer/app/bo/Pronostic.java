package com.pronoplayer.app.bo;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document
public class Pronostic {
    @Id
    private String id;
    private String competitionId;
    private String userId;
    private String groupId;
    private String weekId;
    // private Integer day;
    private List<MatchScore> scores;
}

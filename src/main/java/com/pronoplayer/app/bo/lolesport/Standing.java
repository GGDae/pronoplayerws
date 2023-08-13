package com.pronoplayer.app.bo.lolesport;

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
public class Standing {
    @Id
    public String id;
    public String competitionId;
    public String tournamentId;
    public List<Stage> stages;
}

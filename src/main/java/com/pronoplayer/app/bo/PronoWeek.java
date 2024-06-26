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
public class PronoWeek {
    @Id
    private String id;
    private String competitionId;
    private String block;
    private String period;
    private String lockDate;
    private List<PronoDay> pronoDays;
}

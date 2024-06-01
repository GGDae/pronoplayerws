package com.pronoplayer.app.bo;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document
public class Group {
    @Id
    private String id;
    @JsonIgnore
    private String inviteId;
    private String name;
    private boolean isPublic;
    private List<String> competitions;
    private List<String> administrators;
    private DiscordConfig discord;
}

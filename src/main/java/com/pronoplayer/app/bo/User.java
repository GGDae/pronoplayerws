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
public class User {
    @Id
    private String id;
    private String userId;
    private String login;
    private boolean admin;
    private String displayName;
    private String favouriteTeam;
    private String profileImageUrl;
    private List<Group> groups;
}

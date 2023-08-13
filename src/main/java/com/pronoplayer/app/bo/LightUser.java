package com.pronoplayer.app.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LightUser {
    private String displayName;
    private String favouriteTeam;
    private String profileImageUrl;
}

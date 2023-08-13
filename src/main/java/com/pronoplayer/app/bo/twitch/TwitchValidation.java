package com.pronoplayer.app.bo.twitch;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TwitchValidation {
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("expires_in")
    private Integer expiresIn;
    private String login;
    @JsonProperty("user_id")
    private String userId;
    private TwitchToken renewedToken;
}

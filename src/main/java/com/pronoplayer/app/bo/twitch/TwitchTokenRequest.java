package com.pronoplayer.app.bo.twitch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitchTokenRequest {
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
    private String code;
    @JsonProperty("grant_type")
    private String grantType;
    @JsonProperty("redirect_uri")
    private String redirectUri;
    @JsonProperty("refresh_token")
    private String refreshToken;
}

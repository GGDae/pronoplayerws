package com.pronoplayer.app.bo.twitch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitchData {
    private String id;
    @JsonProperty("display_name")
    private String displayName;
    private String login;
    @JsonProperty("profile_image_url")
    private String profileImageUrl;
}

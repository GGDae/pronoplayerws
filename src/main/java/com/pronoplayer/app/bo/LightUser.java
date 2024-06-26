package com.pronoplayer.app.bo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LightUser {
    private String displayName;
    private String profileImageUrl;
    private List<Badge> badges;
}

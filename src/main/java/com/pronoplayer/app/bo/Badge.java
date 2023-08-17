package com.pronoplayer.app.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Badge {
    private String label;
    private String imageLink;
    private boolean active;
}

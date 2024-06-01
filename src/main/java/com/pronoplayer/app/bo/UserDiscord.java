package com.pronoplayer.app.bo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDiscord {
    
    private String id;
    private String code;
    private Boolean reminder;
    private List<String> competitions;
    private Boolean mpDetail;
}

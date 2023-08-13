package com.pronoplayer.app.bo.lolesport;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Stage {
    public String name;
    public String slug;
    public List<Section> sections;
}

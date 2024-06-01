package com.pronoplayer.app.bo;

import java.util.List;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LightGroup {
    @Id
    private String id;
    private String name;
    private List<String> competitions;

    public LightGroup(Group group) {
        this.id = group.getId();
        this.name = group.getName();
        this.competitions = group.getCompetitions();
    }
}

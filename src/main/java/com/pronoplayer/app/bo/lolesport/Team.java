package com.pronoplayer.app.bo.lolesport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Team{
    public String name;
    public String code;
    public String image;
    public Result result;
    @JsonProperty("record") 
    public Record myrecord;
}

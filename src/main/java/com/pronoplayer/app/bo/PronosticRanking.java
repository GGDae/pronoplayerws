package com.pronoplayer.app.bo;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PronosticRanking {
    private HashMap<String, Integer> ranking;
}

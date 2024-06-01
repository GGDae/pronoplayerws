package com.pronoplayer.app.bo;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Document
public class DiscordConfig {
    private boolean enabled;
    private List<String> competitions;
    private List<DiscordChannel> channels;
}

package com.pronoplayer.app.competition;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CompetitionRestClient {

    @Bean
    public RestTemplate lolesportRestClient() {
        return new RestTemplate();
    }
    
}

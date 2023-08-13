package com.pronoplayer.app.twitch;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.pronoplayer.app.bo.twitch.TwitchData;
import com.pronoplayer.app.bo.twitch.TwitchDataResponse;
import com.pronoplayer.app.bo.twitch.TwitchToken;
import com.pronoplayer.app.bo.twitch.TwitchTokenRequest;
import com.pronoplayer.app.bo.twitch.TwitchValidation;
import com.pronoplayer.app.exceptions.UnauthorizedException;

@Service
public class TwitchService {
    private static final String VALIDATION_URL = "https://id.twitch.tv/oauth2/validate";
    private static final String USER_URL = "https://api.twitch.tv/helix/users";
    private static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";
    private final RestTemplate restTemplate;
    @Value("${app.twitch.client-id}")
    private String clientId;
    @Value("${app.twitch.client-secret}")
    private String secret;
    
    public TwitchService() {
        this.restTemplate = new RestTemplate();
    }
    
    public TwitchValidation validateToken(String token, String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<TwitchValidation> response = restTemplate.exchange(
            VALIDATION_URL,
            HttpMethod.GET,
            entity,
            TwitchValidation.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
        } catch (HttpClientErrorException.Unauthorized exception) {
            TwitchToken refreshedToken = refreshToken(refreshToken);
            headers.set("Authorization", "Bearer " + refreshedToken.getAccessToken());
            try {
                ResponseEntity<TwitchValidation> response2 = restTemplate.exchange(
                VALIDATION_URL,
                HttpMethod.GET,
                entity,
                TwitchValidation.class
                );
                if (response2.getStatusCode() == HttpStatus.OK) {
                    TwitchValidation twitchValidation = response2.getBody();
                    twitchValidation.setRenewedToken(refreshedToken);
                    return twitchValidation;
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to call Twitch API.");
            }
        }
        throw new RuntimeException("Failed to call Twitch API.");
    }
    
    public TwitchToken getToken(String code, String redirectUri) {
        TwitchTokenRequest req = new TwitchTokenRequest();
        req.setClientId(this.clientId);
        req.setClientSecret(this.secret);
        req.setCode(code);
        req.setGrantType("authorization_code");
        req.setRedirectUri(redirectUri);
        HttpEntity<?> entity = new HttpEntity<>(req);
        ResponseEntity<TwitchToken> response = restTemplate.exchange(
        TOKEN_URL,
        HttpMethod.POST,
        entity,
        TwitchToken.class
        );        
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Failed to call Twitch API.");
        }
    }
    
    public TwitchToken refreshToken(String refreshToken) {
        TwitchTokenRequest req = new TwitchTokenRequest();
        req.setClientId(this.clientId);
        req.setClientSecret(this.secret);
        req.setGrantType("refresh_token");
        req.setRefreshToken(refreshToken);
        HttpEntity<?> entity = new HttpEntity<>(req);
        try {
            ResponseEntity<TwitchToken> response = restTemplate.exchange(
            TOKEN_URL,
            HttpMethod.POST,
            entity,
            TwitchToken.class
            );        
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } 
        } catch (HttpClientErrorException.BadRequest | HttpClientErrorException.Unauthorized exception) {
            throw new UnauthorizedException("access token and refresh token expired.");
        }
        throw new RuntimeException("Failed to call Twitch API.");
        
    }
    
    public TwitchData getTwitchUser(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Client-ID", this.clientId);
        headers.set("Authorization", token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<TwitchDataResponse> response = restTemplate.exchange(
        USER_URL,
        HttpMethod.GET,
        entity,
        TwitchDataResponse.class
        );
        if (response.getStatusCode() == HttpStatus.OK && response.getBody().getData() != null && response.getBody().getData().size() > 0) {
            return response.getBody().getData().get(0);
        } else {
            throw new RuntimeException("Failed to call Twitch API.");
        }
    }
}

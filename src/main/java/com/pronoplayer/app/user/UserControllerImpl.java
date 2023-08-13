package com.pronoplayer.app.user;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.pronoplayer.app.bo.LightUser;
import com.pronoplayer.app.bo.User;
import com.pronoplayer.app.bo.twitch.TwitchToken;
import com.pronoplayer.app.bo.twitch.TwitchValidation;
import com.pronoplayer.app.exceptions.UnauthorizedException;
import com.pronoplayer.app.twitch.TwitchService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class UserControllerImpl implements UserController {
    
    private final UserService userService;
    private final TwitchService twitchService;
    
    @Override
    public User getUser(String userId, String token, String refreshToken) {
        TwitchValidation validation = twitchService.validateToken(token, refreshToken);
        if (validation != null && validation.getUserId().equals(userId)) { 
            return userService.getUserByUserId(userId);
        }
        throw new UnauthorizedException("Cannot access foreign user data");
    }
    
    public LightUser getLightUser(String userId) {
        User user = userService.getUserByUserId(userId);
        LightUser lightUser = new LightUser();
        lightUser.setDisplayName(user.getDisplayName());
        lightUser.setFavouriteTeam(user.getFavouriteTeam());
        lightUser.setProfileImageUrl(user.getProfileImageUrl());
        return lightUser;
    }
    
    @Override
    public TwitchToken getToken(String code, String redirectUri) {
        return twitchService.getToken(code, redirectUri);
    }
    
    @Override
    public ResponseEntity<User> getUserFromToken(String token, String refreshToken) {
        TwitchValidation validation = twitchService.validateToken(token, refreshToken);
        if (validation != null) {
            User user = userService.getUserFromToken(validation.getUserId(), token);
            HttpHeaders headers = new HttpHeaders();
            if (validation.getRenewedToken() != null) {
                headers.add("access_token", validation.getRenewedToken().getAccessToken());
                headers.add("refresh_token", validation.getRenewedToken().getRefreshToken());
            }
            return new ResponseEntity<User>(user, headers, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
}

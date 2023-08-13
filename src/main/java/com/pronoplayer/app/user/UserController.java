package com.pronoplayer.app.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pronoplayer.app.bo.LightUser;
import com.pronoplayer.app.bo.User;
import com.pronoplayer.app.bo.twitch.TwitchToken;

@RestController
@RequestMapping(value = "/api/user")
public interface UserController {

    @GetMapping("/token")
    public TwitchToken getToken(@RequestParam("code") String code, @RequestParam("redirect_uri") String redirectUri);

    @GetMapping("/{userId}")
    public User getUser(@PathVariable("userId") String userId, @RequestHeader("Authorization") String token, @RequestHeader("Refreshtoken") String refreshToken);
    
    @GetMapping("/{userId}/light")
    public LightUser getLightUser(@PathVariable("userId") String userId);

    @GetMapping()
    public ResponseEntity<User> getUserFromToken(@RequestHeader("Authorization") String token, @RequestHeader("Refreshtoken") String refreshToken);
}

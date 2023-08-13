package com.pronoplayer.app.user;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pronoplayer.app.bo.User;
import com.pronoplayer.app.bo.twitch.TwitchData;
import com.pronoplayer.app.twitch.TwitchService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TwitchService twitchService;
    
    @Override
    public User getUserByUserId(String userId) {
        return userRepository.findByUserId(userId).orElse(null);
    }
    
    @Override
    public User getUserFromToken(String userId, String token) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            TwitchData twitchUser = twitchService.getTwitchUser(token);
            return createUser(twitchUser);
        }
        return userOpt.get();
    }
    
    private User createUser(TwitchData twitchData) {
        User user = new User();
        user.setUserId(twitchData.getId());
        user.setLogin(twitchData.getLogin());
        user.setDisplayName(twitchData.getDisplayName());
        user.setProfileImageUrl(twitchData.getProfileImageUrl());
        return userRepository.save(user);
    }
}

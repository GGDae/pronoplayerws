package com.pronoplayer.app.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.pronoplayer.app.bo.Badge;
import com.pronoplayer.app.bo.User;
import com.pronoplayer.app.bo.UserDiscord;
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
    public User updateUser(User user) {
        User localUser = getUserByUserId(user.getUserId());
        if (localUser != null) {
            if (user.getBadges() != null) {
                List<Badge> badges = user.getBadges().stream().filter(badge -> badge.getImageLink().startsWith("assets/badges") && badge.getImageLink().endsWith(".png")).toList();
                localUser.setBadges(badges);
            }
            if (localUser.getDiscord() == null && user.getDiscord() != null) {
                localUser.setDiscord(new UserDiscord());
            }
            if (user.getDiscord() != null) {
                localUser.getDiscord().setCode(user.getDiscord().getCode());
                if (user.getDiscord().getCompetitions() != null) {
                    localUser.getDiscord().setCompetitions(user.getDiscord().getCompetitions());
                }
            }
            return userRepository.save(localUser);
        }
        return null;
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
    
    @Override
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }
    
    @Override
    public List<User> getAllUsersWithReminder() {
        return this.userRepository.findByDiscordReminder(true).orElse(new ArrayList<>());
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

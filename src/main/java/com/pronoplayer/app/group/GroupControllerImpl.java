package com.pronoplayer.app.group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.pronoplayer.app.bo.Group;
import com.pronoplayer.app.bo.LightGroup;
import com.pronoplayer.app.bo.twitch.TwitchValidation;
import com.pronoplayer.app.twitch.TwitchService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class GroupControllerImpl implements GroupController {
    
    private final GroupService groupService;
    private final TwitchService twitchService;
    
    public List<Group> findAll() {
        return groupService.findAll();
    }
    
    @Override
    public Group getGroup(String groupId) {
        return groupService.getGroup(groupId);
    }
    @Override
    public Group getGroupByIdOrInviteId(String id) {
        return groupService.getGroupByIdOrInviteId(id);
    }
    
    @Override
    public ResponseEntity<Group> joinGroup(String groupId, String userId, String token, String refreshToken) {
        TwitchValidation validation = twitchService.validateToken(token, refreshToken);
        if (validation != null && validation.getUserId().equals(userId)) {
            Group group = groupService.joinGroup(groupId, userId);
            HttpHeaders headers = new HttpHeaders();
            if (validation.getRenewedToken() != null) {
                headers.add("access_token", validation.getRenewedToken().getAccessToken());
                headers.add("refresh_token", validation.getRenewedToken().getRefreshToken());
            }
            return new ResponseEntity<Group>(group, headers, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
    
    @Override
    public List<Group> getManagedGroups(String userId, String token, String refreshToken) {
        TwitchValidation validation = twitchService.validateToken(token, refreshToken);
        if (validation != null && validation.getUserId().equals(userId)) {
            return groupService.getManagedGroups(userId);
        }
        return new ArrayList<>();
    }
    
    @Override
    public Group addCompetitionToGroup(String groupId, String competitionId, String userId, String token, String refreshToken) {
        TwitchValidation validation = twitchService.validateToken(token, refreshToken);
        if (validation != null && validation.getUserId().equals(userId)) {
            return groupService.addCompetitionToGroup(groupId, competitionId, userId);
        }
        return null;
    }
    
    @Override
    public Group removeCompetitionFromGroup(String groupId, String competitionId, String userId, String token, String refreshToken) {
        TwitchValidation validation = twitchService.validateToken(token, refreshToken);
        if (validation != null && validation.getUserId().equals(userId)) {
            return groupService.removeCompetitionFromGroup(groupId, competitionId, userId);
        }
        return null;
    }
        
    @Override
    public Group addDiscordNotificationForCompetition(String groupId, String competitionId, String userId, String token, String refreshToken) {
        TwitchValidation validation = twitchService.validateToken(token, refreshToken);
        if (validation != null && validation.getUserId().equals(userId)) {
            return groupService.addDiscordNotificationForCompetition(groupId, competitionId, userId);
        }
        return null;
    }
    
    @Override
    public Group removeDiscordNotificationForCompetition(String groupId, String competitionId, String userId, String token, String refreshToken) {
        TwitchValidation validation = twitchService.validateToken(token, refreshToken);
        if (validation != null && validation.getUserId().equals(userId)) {
            return groupService.removeDiscordNotificationForCompetition(groupId, competitionId, userId);
        }
        return null;
    }
    
    @Override
    public ResponseEntity<Map<String, String>> getInviteId(String groupId, String userId, String token, String refreshToken) {
        TwitchValidation validation = twitchService.validateToken(token, refreshToken);
        if (validation != null && validation.getUserId().equals(userId)) {
            Map<String, String> response = new HashMap<>();
            response.put("data", groupService.getInviteId(groupId, userId));
            return ResponseEntity.ok(response);
        }
        return null;
    }

    @Override
    public List<LightGroup> getPublicGroups() {
        return groupService.getPublicGroups();
    }
    
}

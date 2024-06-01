package com.pronoplayer.app.group;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pronoplayer.app.bo.Group;
import com.pronoplayer.app.bo.LightGroup;

@RestController
@RequestMapping(value = "/api/group")
public interface GroupController {

    @GetMapping
    public List<Group> findAll();

    @GetMapping("/join/{groupId}")
    public ResponseEntity<Group> joinGroup(@PathVariable("groupId") String groupId, @RequestParam("userId") String userId, @RequestHeader("Authorization") String token, @RequestHeader("refreshtoken") String refreshToken);

    @GetMapping("/managed/{userId}")
    public List<Group> getManagedGroups(@PathVariable("userId") String userId, @RequestHeader("Authorization") String token, @RequestHeader("refreshtoken") String refreshToken);

    @GetMapping("/{groupId}")
    public Group getGroup(@PathVariable("groupId") String groupId);

    @GetMapping("/invite/{id}")
    public Group getGroupByIdOrInviteId(@PathVariable("id") String id);

    @GetMapping("/{groupId}/addCompetition")
    public Group addCompetitionToGroup(@PathVariable("groupId") String groupId, @RequestParam("competitionId") String competitionId, @RequestParam("userId") String userId, @RequestHeader("Authorization") String token, @RequestHeader("refreshtoken") String refreshToken);

    @GetMapping("/{groupId}/removeCompetition")
    public Group removeCompetitionFromGroup(@PathVariable("groupId") String groupId, @RequestParam("competitionId") String competitionId, @RequestParam("userId") String userId, @RequestHeader("Authorization") String token, @RequestHeader("refreshtoken") String refreshToken);

    @GetMapping("/{groupId}/addNotification")
    public Group addDiscordNotificationForCompetition(@PathVariable("groupId") String groupId, @RequestParam("competitionId") String competitionId, @RequestParam("userId") String userId, @RequestHeader("Authorization") String token, @RequestHeader("refreshtoken") String refreshToken);

    @GetMapping("/{groupId}/removeNotification")
    public Group removeDiscordNotificationForCompetition(@PathVariable("groupId") String groupId, @RequestParam("competitionId") String competitionId, @RequestParam("userId") String userId, @RequestHeader("Authorization") String token, @RequestHeader("refreshtoken") String refreshToken);

    @GetMapping("/{groupId}/inviteId")
    public ResponseEntity<Map<String, String>> getInviteId(@PathVariable("groupId") String groupId,@RequestParam("userId") String userId, @RequestHeader("Authorization") String token, @RequestHeader("refreshtoken") String refreshToken);
    
    @GetMapping("/public")
    public List<LightGroup> getPublicGroups();

}

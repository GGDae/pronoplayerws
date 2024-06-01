package com.pronoplayer.app.group;

import java.util.List;

import com.pronoplayer.app.bo.Group;
import com.pronoplayer.app.bo.LightGroup;

public interface GroupService {
    public List<Group> findAll();
    public Group save(Group group);
    public Group getGroup(String groupId);
    public Group getGroupByName(String groupName);
    public Group joinGroup(String groupId, String userId);
    public Group getGroupByIdOrInviteId(String id);
    public List<Group> getManagedGroups(String userId);
    public String getInviteId(String groupId, String userId);
    public Group addCompetitionToGroup(String groupId, String competitionId, String userId);
    public Group removeCompetitionFromGroup(String groupId, String competitionId, String userId);
    public Group addDiscordNotificationForCompetition(String groupId, String competitionId, String userId);
    public Group removeDiscordNotificationForCompetition(String groupId, String competitionId, String userId);
    public List<LightGroup> getPublicGroups();
    public boolean isAdministrator(String userId, String groupId);
}

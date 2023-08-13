package com.pronoplayer.app.group;

import java.util.List;

import com.pronoplayer.app.bo.Group;

public interface GroupService {
    public List<Group> findAll();
    public Group getGroup(String groupId);
    public Group joinGroup(String groupId, String userId);
    public Group getGroupByIdOrInviteId(String id);
    public List<Group> getManagedGroups(String userId);
    public String getInviteId(String groupId, String userId);
    public Group addCompetitionToGroup(String groupId, String competitionId, String userId);
    public Group removeCompetitionFromGroup(String groupId, String competitionId, String userId);
}

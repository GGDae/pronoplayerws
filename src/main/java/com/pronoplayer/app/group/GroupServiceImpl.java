package com.pronoplayer.app.group;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.pronoplayer.app.bo.DiscordConfig;
import com.pronoplayer.app.bo.Group;
import com.pronoplayer.app.bo.LightGroup;
import com.pronoplayer.app.bo.User;
import com.pronoplayer.app.exceptions.ForbiddenException;
import com.pronoplayer.app.exceptions.NotFoundException;
import com.pronoplayer.app.user.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    
    @Override
    public List<Group> findAll() {
        return groupRepository.findAll();
    }
    
    @Override
    public Group save(Group group) {
        return groupRepository.save(group);
    }
    
    @Override
    public Group getGroup(String groupId) {
        return groupRepository.findById(groupId).orElse(null);
    }

    @Override
    public Group getGroupByName(String groupName) {
        return groupRepository.findByName(groupName);
    }
    
    @Override
    public Group getGroupByIdOrInviteId(String id) {
        return groupRepository.findFirstByIdOrInviteId(id, id).orElse(null);
    }
    
    @Override
    public Group joinGroup(String groupId, String userId) {
        Group group = getGroupByIdOrInviteId(groupId);
        if (group == null) {
            throw new NotFoundException("Group does not exist");
        }
        User user = userRepository.findByUserId(userId).get();
        if (user.getGroups() == null) {
            user.setGroups(new ArrayList<>());
        }
        user.getGroups().add(group);
        userRepository.save(user);
        return group;
    }
    
    @Override
    public List<Group> getManagedGroups(String userId) {
        return groupRepository.findByAdministrators(userId).orElse(new ArrayList<>());
    }
    
    @Override
    public String getInviteId(String groupId, String userId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group != null && group.getAdministrators().contains(userId)) {
            return group.getInviteId();
        }
        throw new ForbiddenException("user is not a group administrator");
    }
    
    @Override
    public Group addCompetitionToGroup(String groupId, String competitionId, String userId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group != null && group.getAdministrators().contains(userId)) {
            group.getCompetitions().add(competitionId);
            return groupRepository.save(group);
        }
        return null;
    }
    
    @Override
    public Group removeCompetitionFromGroup(String groupId, String competitionId, String userId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group != null && group.getAdministrators().contains(userId)) {
            group.setCompetitions(group.getCompetitions().stream().filter(comp -> !comp.equals(competitionId)).toList());
            return groupRepository.save(group);
        }
        return null;
    }
    
    @Override
    public Group addDiscordNotificationForCompetition(String groupId, String competitionId, String userId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group != null && group.getAdministrators().contains(userId)) {
            if (group.getDiscord() == null) {
                group.setDiscord(new DiscordConfig());
            }
            if (group.getDiscord().getCompetitions() == null) {
                group.getDiscord().setCompetitions(new ArrayList<>());
            }
            if (!group.getDiscord().getCompetitions().contains(competitionId)) {
                group.getDiscord().getCompetitions().add(competitionId);
            }
            return groupRepository.save(group);
        }
        return null;
    }
    
    @Override
    public Group removeDiscordNotificationForCompetition(String groupId, String competitionId, String userId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group != null && group.getAdministrators().contains(userId)) {
            group.getDiscord().setCompetitions(group.getDiscord().getCompetitions().stream().filter(comp -> !comp.equals(competitionId)).toList());
            return groupRepository.save(group);
        }
        return null;
    }

    @Override
    @Cacheable(cacheNames = "publicGroups")
    public List<LightGroup> getPublicGroups() {
        List<Group> groups = groupRepository.findByIsPublic(true);
        return groups.stream().map(LightGroup::new).toList();
    }

    @Override
    public boolean isAdministrator(String userId, String groupId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        return group != null && group.getAdministrators().contains(userId);
    }
    
}

package com.pronoplayer.app.group;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.pronoplayer.app.bo.Group;
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
    public Group getGroup(String groupId) {
        return groupRepository.findById(groupId).orElse(null);
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
            group.setCompetitions(group.getCompetitions().stream().filter(comp -> !comp.equals(competitionId)).collect(Collectors.toList()));
            return groupRepository.save(group);
        }
        return null;
    }
    
}

package com.pronoplayer.app.group;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.pronoplayer.app.bo.Group;


public interface GroupRepository extends MongoRepository<Group, String> {
    public Optional<Group> findFirstByIdOrInviteId(String id, String inviteId);
    
    @Aggregation(pipeline = {
        "{$match: {administrators: ?0}}"
    })
    public Optional<List<Group>> findByAdministrators(String userId);
}

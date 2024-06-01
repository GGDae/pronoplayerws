package com.pronoplayer.app.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pronoplayer.app.bo.User;


public interface UserRepository extends MongoRepository<User, String> {
    public Optional<User> findByUserId(String userId);
    public Optional<User> findByDiscordId(String discordId);
    public Optional<User> findByDiscordCode(String discordCode);
    public Optional<List<User>> findByDiscordReminder(boolean reminder);
}

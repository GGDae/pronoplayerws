package com.pronoplayer.app.user;

import java.util.List;

import com.pronoplayer.app.bo.User;

public interface UserService {
    public User getUserByUserId(String userId);
    public User getUserFromToken(String token, String refreshToken);
    public User updateUser(User user);
    public List<User> getAllUsers();
    public List<User> getAllUsersWithReminder();
}

package com.pronoplayer.app.user;

import com.pronoplayer.app.bo.User;

public interface UserService {
    public User getUserByUserId(String userId);
    public User getUserFromToken(String token, String refreshToken);
}

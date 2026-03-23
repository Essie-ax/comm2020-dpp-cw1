package uk.ac.comm2020.service;

import uk.ac.comm2020.dao.UserDao;
import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.model.User;
import uk.ac.comm2020.util.ApiResponse;
import uk.ac.comm2020.util.PasswordUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SessionService {

    private final UserDao userDao;
    private final Map<String, Session> sessions = new HashMap<>();

    public SessionService(UserDao userDao) {
        this.userDao = userDao;
    }

    public ApiResponse authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null) {
            return ApiResponse.error("UNAUTHORIZED", "Invalid credentials");
        }

        Optional<User> opt = userDao.findByUsername(username.trim());
        if (opt.isEmpty()) {
            return ApiResponse.error("UNAUTHORIZED", "Invalid credentials");
        }

        User user = opt.get();
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            return ApiResponse.error("UNAUTHORIZED", "Invalid credentials");
        }

        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(user.getId(), user.getRole(), user.getUsername()));

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("role", user.getRole().name());
        data.put("userId", user.getId());

        return ApiResponse.ok(data);
    }

    public Session validateToken(String token) {
        return sessions.get(token);
    }

    public static class Session {
        public final long userId;
        public final Role role;
        public final String username;

        public Session(long userId, Role role, String username) {
            this.userId = userId;
            this.role = role;
            this.username = username;
        }
    }
}

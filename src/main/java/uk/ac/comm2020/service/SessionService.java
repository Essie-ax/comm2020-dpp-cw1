package uk.ac.comm2020.service;

import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.util.ApiResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SessionService {

    private final Map<String, Session> sessions = new HashMap<>();

    public ApiResponse authenticate(String username, String password) {
        Role role;
        long userId;

        if ("player1".equals(username) && "password".equals(password)) {
            role = Role.PLAYER;
            userId = 1;
        } else if ("gamekeeper1".equals(username) && "password".equals(password)) {
            role = Role.GAME_KEEPER;
            userId = 2;
        } else {
            return ApiResponse.error("UNAUTHORIZED", "Invalid credentials");
        }

        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(userId, role, username));

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("role", role.name());
        data.put("userId", userId);

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
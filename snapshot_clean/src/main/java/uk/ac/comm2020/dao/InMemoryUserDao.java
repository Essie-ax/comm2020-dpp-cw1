package uk.ac.comm2020.dao;

import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.model.User;
import uk.ac.comm2020.util.PasswordUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory user store for tests and when DB_URL is not set.
 * Demo users: player1 (PLAYER), gamekeeper1 (GAME_KEEPER), password = "password".
 */
public class InMemoryUserDao implements UserDao {

    private static final String DEMO_PASSWORD_HASH = PasswordUtil.hash("password");

    private final Map<String, User> users = new HashMap<>();

    public InMemoryUserDao() {
        users.put("player1", new User(1, "player1", DEMO_PASSWORD_HASH, Role.PLAYER));
        users.put("gamekeeper1", new User(2, "gamekeeper1", DEMO_PASSWORD_HASH, Role.GAME_KEEPER));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username != null ? username.trim() : null));
    }
}

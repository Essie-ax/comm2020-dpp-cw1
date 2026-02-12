package uk.ac.comm2020.dao;

import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.model.User;
import uk.ac.comm2020.util.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class MySqlUserDao implements UserDao {

    @Override
    public Optional<User> findByUsername(String username) {
        if (username == null || username.isBlank()) return Optional.empty();
        String trimmed = username.trim();

        try (Connection c = Db.getConnection()) {
            if (c == null) return Optional.empty();
            String sql = "SELECT user_id, username, password_hash, role FROM users WHERE username = ? LIMIT 1";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, trimmed);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        long id = rs.getLong("user_id");
                        String un = rs.getString("username");
                        String hash = rs.getString("password_hash");
                        String roleStr = rs.getString("role");
                        Role role = roleFromDb(roleStr);
                        return Optional.of(new User(id, un, hash, role));
                    }
                }
            }
        } catch (SQLException e) {
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static Role roleFromDb(String roleStr) {
        if (roleStr == null) return Role.PLAYER;
        switch (roleStr.toUpperCase()) {
            case "GAMEKEEPER":
                return Role.GAME_KEEPER;
            case "PLAYER":
            default:
                return Role.PLAYER;
        }
    }
}

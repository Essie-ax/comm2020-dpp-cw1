package uk.ac.comm2020.model;

public class User {

    private final long id;
    private final String username;
    private final String passwordHash;
    private final Role role;

    public User(long id, String username, String passwordHash, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash != null ? passwordHash : "";
        this.role = role;
    }

    public User(String username, Role role) {
        this(0, username, null, role);
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }
}

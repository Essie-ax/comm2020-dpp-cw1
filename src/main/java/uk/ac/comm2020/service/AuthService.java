package uk.ac.comm2020.service;

import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.model.User;

public class AuthService {

    private User currentUser;

    public void login(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void requireGameKeeper() {
        if (currentUser == null) {
            throw new IllegalStateException("No user logged in");
        }
        if (currentUser.getRole() != Role.GAME_KEEPER) {
            throw new SecurityException("Only GameKeeper can perform this action");
        }
    }
}


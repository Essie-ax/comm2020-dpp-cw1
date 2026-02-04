package uk.ac.comm2020;

import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.model.User;
import uk.ac.comm2020.service.AuthService;

public class App {

    public static void main(String[] args) {

        AuthService authService = new AuthService();

        User player = new User("alice", Role.PLAYER);
        User gameKeeper = new User("bob", Role.GAME_KEEPER);

        // Login as Player
        authService.login(player);

        try {
            authService.requireGameKeeper();
            System.out.println("Player is allowed to perform GameKeeper action");
        } catch (Exception e) {
            System.out.println("Player denied: " + e.getMessage());
        }

        // Login as GameKeeper
        authService.login(gameKeeper);

        try {
            authService.requireGameKeeper();
            System.out.println("GameKeeper allowed to perform action");
        } catch (Exception e) {
            System.out.println("GameKeeper denied: " + e.getMessage());
        }
    }
}

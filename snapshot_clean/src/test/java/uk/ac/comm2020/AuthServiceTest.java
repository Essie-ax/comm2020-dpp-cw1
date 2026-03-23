package uk.ac.comm2020;

import org.junit.jupiter.api.Test;
import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.model.User;
import uk.ac.comm2020.service.AuthService;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @Test
    void playerShouldNotBeAllowedToPerformGameKeeperAction() {
        AuthService authService = new AuthService();
        User player = new User("alice", Role.PLAYER);

        authService.login(player);

        assertThrows(SecurityException.class, authService::requireGameKeeper);
    }

    @Test
    void gameKeeperShouldBeAllowedToPerformGameKeeperAction() {
        AuthService authService = new AuthService();
        User gameKeeper = new User("bob", Role.GAME_KEEPER);

        authService.login(gameKeeper);

        assertDoesNotThrow(authService::requireGameKeeper);
    }
}


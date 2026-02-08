package uk.ac.comm2020;

import org.junit.jupiter.api.Test;
import uk.ac.comm2020.dao.InMemoryUserDao;
import uk.ac.comm2020.model.Role;
import uk.ac.comm2020.service.SessionService;
import uk.ac.comm2020.util.ApiResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auth with InMemoryUserDao (no DB). Same usernames/password as before: player1, gamekeeper1, password="password".
 */
public class SessionServiceTest {

    @Test
    void loginWithPlayer1AndPasswordReturnsTokenAndPlayerRole() {
        SessionService service = new SessionService(new InMemoryUserDao());
        ApiResponse res = service.authenticate("player1", "password");
        assertTrue(res.isSuccess());
        assertEquals(200, res.getStatus());
        assertEquals("PLAYER", res.getData().get("role"));
        assertNotNull(res.getData().get("token"));
        assertEquals(1L, res.getData().get("userId"));
    }

    @Test
    void loginWithGamekeeper1AndPasswordReturnsTokenAndGameKeeperRole() {
        SessionService service = new SessionService(new InMemoryUserDao());
        ApiResponse res = service.authenticate("gamekeeper1", "password");
        assertTrue(res.isSuccess());
        assertEquals(200, res.getStatus());
        assertEquals("GAME_KEEPER", res.getData().get("role"));
        assertNotNull(res.getData().get("token"));
        assertEquals(2L, res.getData().get("userId"));
    }

    @Test
    void invalidPasswordReturnsUnauthorized() {
        SessionService service = new SessionService(new InMemoryUserDao());
        ApiResponse res = service.authenticate("player1", "wrong");
        assertFalse(res.isSuccess());
        assertEquals(401, res.getStatus());
    }

    @Test
    void unknownUserReturnsUnauthorized() {
        SessionService service = new SessionService(new InMemoryUserDao());
        ApiResponse res = service.authenticate("nobody", "password");
        assertFalse(res.isSuccess());
        assertEquals(401, res.getStatus());
    }

    @Test
    void validateTokenReturnsSessionAfterLogin() {
        SessionService service = new SessionService(new InMemoryUserDao());
        ApiResponse res = service.authenticate("gamekeeper1", "password");
        String token = (String) res.getData().get("token");
        SessionService.Session session = service.validateToken(token);
        assertNotNull(session);
        assertEquals(2L, session.userId);
        assertEquals(Role.GAME_KEEPER, session.role);
        assertEquals("gamekeeper1", session.username);
    }
}

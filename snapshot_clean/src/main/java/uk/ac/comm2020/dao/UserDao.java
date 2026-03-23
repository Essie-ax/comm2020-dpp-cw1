package uk.ac.comm2020.dao;

import uk.ac.comm2020.model.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username);
}

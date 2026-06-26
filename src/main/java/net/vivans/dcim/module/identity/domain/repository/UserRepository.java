package net.vivans.dcim.module.identity.domain.repository;

import net.vivans.dcim.module.identity.domain.model.User;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findByUsername(String username);

    Optional<User> findByRefreshToken(String refreshToken);

    User save(User user);
}

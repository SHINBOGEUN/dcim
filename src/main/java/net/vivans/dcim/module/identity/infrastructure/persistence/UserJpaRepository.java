package net.vivans.dcim.module.identity.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import net.vivans.dcim.module.identity.domain.model.User;
import net.vivans.dcim.module.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserJpaRepository implements UserRepository {

    private final UserDataRepository springDataRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByRefreshToken(String refreshToken) {
        return springDataRepository.findByRefreshToken(refreshToken);
    }

    @Override
    public User save(User user) {
        return springDataRepository.save(user);
    }
}

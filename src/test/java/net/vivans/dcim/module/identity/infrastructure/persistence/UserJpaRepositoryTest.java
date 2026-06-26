package net.vivans.dcim.module.identity.infrastructure.persistence;

import net.vivans.dcim.bootstrap.ManagerServerApplication;
import net.vivans.dcim.module.identity.domain.model.User;
import net.vivans.dcim.module.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ManagerServerApplication.class)
@ActiveProfiles("local")
@Transactional
class UserJpaRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindByUsername() {
        User user = User.createNew("repo-user", "encoded-password");

        userRepository.save(user);

        assertThat(userRepository.findByUsername("repo-user"))
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getId()).isNotNull();
                    assertThat(found.getUsername()).isEqualTo("repo-user");
                    assertThat(found.getCreatedDt()).isNotNull();
                    assertThat(found.getUpdatedDt()).isNotNull();
                });
    }

    @Test
    void findByRefreshToken() {
        User user = User.createNew("refresh-user", "encoded-password");
        user.updateRefreshToken("refresh-token-value");
        userRepository.save(user);

        assertThat(userRepository.findByRefreshToken("refresh-token-value"))
                .isPresent()
                .get()
                .extracting(User::getUsername)
                .isEqualTo("refresh-user");
    }

}

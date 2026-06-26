package net.vivans.dcim.module.identity.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void createNew_createsUserWithDefaultRole() {
        User user = User.createNew("testuser", "encoded-password");

        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getRole()).isEqualTo("USER");
        assertThat(user.getRefreshToken()).isNull();
    }

    @Test
    void createNew_throwsWhenUsernameIsBlank() {
        assertThatThrownBy(() -> User.createNew("  ", "encoded-password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("username is required");
    }

    @Test
    void createNew_throwsWhenPasswordIsBlank() {
        assertThatThrownBy(() -> User.createNew("testuser", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("password is required");
    }

    @Test
    void updateRefreshToken_updatesToken() {
        User user = User.createNew("testuser", "encoded-password");

        user.updateRefreshToken("refresh-token");

        assertThat(user.getRefreshToken()).isEqualTo("refresh-token");
    }

}

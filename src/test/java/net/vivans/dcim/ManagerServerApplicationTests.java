package net.vivans.dcim;

import net.vivans.dcim.bootstrap.ManagerServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ManagerServerApplication.class)
@ActiveProfiles("local")
class ManagerServerApplicationTests {

    @Test
    void contextLoads() {
    }

}

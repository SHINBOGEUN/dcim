package net.vivans.dcim.module.identity.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.vivans.dcim.bootstrap.ManagerServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ManagerServerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_returnsCreatedUser() throws Exception {
        mockMvc.perform(post("/api/manager/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"register-user","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.username").value("register-user"))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void login_returnsTokens() throws Exception {
        registerUser("login-user", "password123");

        mockMvc.perform(post("/api/manager/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"login-user","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("login-user"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    @Test
    void login_returnsUnauthorizedForWrongPassword() throws Exception {
        registerUser("wrong-pass-user", "password123");

        mockMvc.perform(post("/api/manager/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"wrong-pass-user","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void validate_returnsTokenInfo() throws Exception {
        JsonNode loginResponse = loginAndGetResponse("validate-user", "password123");
        String accessToken = loginResponse.get("accessToken").asText();

        mockMvc.perform(get("/api/manager/auth/validate")
                        .param("accessToken", accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("validate-user"))
                .andExpect(jsonPath("$.data.accessToken").value(accessToken));
    }

    @Test
    void refresh_returnsNewTokens() throws Exception {
        JsonNode loginResponse = loginAndGetResponse("refresh-user", "password123");
        String refreshToken = loginResponse.get("refreshToken").asText();

        mockMvc.perform(post("/api/manager/auth/refresh")
                        .param("refreshToken", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("refresh-user"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }

    private void registerUser(String username, String password) throws Exception {
        mockMvc.perform(post("/api/manager/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"%s","password":"%s"}
                        """.formatted(username, password)));
    }

    private JsonNode loginAndGetResponse(String username, String password) throws Exception {
        registerUser(username, password);

        MvcResult result = mockMvc.perform(post("/api/manager/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }

}

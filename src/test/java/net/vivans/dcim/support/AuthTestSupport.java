package net.vivans.dcim.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class AuthTestSupport {

    private AuthTestSupport() {
    }

    public static void registerUser(MockMvc mockMvc, String username, String password) throws Exception {
        mockMvc.perform(post("/api/manager/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"%s","password":"%s"}
                        """.formatted(username, password)));
    }

    public static JsonNode loginAndGetResponse(MockMvc mockMvc, ObjectMapper objectMapper, String username, String password) throws Exception {
        registerUser(mockMvc, username, password);

        MvcResult result = mockMvc.perform(post("/api/manager/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
    }

    public static String loginAndGetAccessToken(MockMvc mockMvc, ObjectMapper objectMapper, String username, String password) throws Exception {
        return loginAndGetResponse(mockMvc, objectMapper, username, password).get("accessToken").asText();
    }

    public static String bearerToken(String accessToken) {
        return "Bearer " + accessToken;
    }
}

package net.vivans.dcim.module.common.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.vivans.dcim.bootstrap.ManagerServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static net.vivans.dcim.support.AuthTestSupport.loginAndGetAccessToken;
import static net.vivans.dcim.support.AuthTestSupport.bearerToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ManagerServerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
public class CommonCodeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_returnCommonCodeResponse() throws Exception{
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "common-code-user", "password123");

        String groupResponse = mockMvc.perform(post("/api/manager/code-groups")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"groupKey": "DEVICE_TYPE", "groupName": "장비 유형"}
                        """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Integer groupId = objectMapper.readTree(groupResponse).path("data").path("id").asInt();
        mockMvc.perform(post("/api/manager/common-codes")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"groupId" : %d, "code": "pdu", "name": "pdu", "sortOrder": 1}
                        """.formatted(groupId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.groupId").value(groupId))
                .andExpect(jsonPath("$.data.code").value("pdu"))
                .andExpect(jsonPath("$.data.name").value("pdu"))
                .andExpect(jsonPath("$.data.sortOrder").value(1));
    }
}

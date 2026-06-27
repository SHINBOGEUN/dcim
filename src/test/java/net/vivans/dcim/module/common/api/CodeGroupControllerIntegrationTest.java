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

import static net.vivans.dcim.support.AuthTestSupport.bearerToken;
import static net.vivans.dcim.support.AuthTestSupport.loginAndGetAccessToken;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ManagerServerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class CodeGroupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_returnCodeGroup() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "codegroup-user", "password123");

        mockMvc.perform(post("/api/manager/code-groups")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"groupKey": "Test_Type", "groupName": "TEST"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.groupKey").value("Test_Type"))
                .andExpect(jsonPath("$.data.groupName").value("TEST"));
    }

    @Test
    void getCodeGroupList_returnCodeGroupList() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "codegroup-list-user", "password123");

        createCodeGroup(accessToken, "DEVICE_TYPE", "장비 유형");
        createCodeGroup(accessToken, "SENSOR_TYPE", "센서 유형");
        createCodeGroup(accessToken, "LOCATION_TYPE", "위치 유형");

        mockMvc.perform(get("/api/manager/code-groups")
                        .header("Authorization", bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[*].groupKey").value(hasItems("DEVICE_TYPE", "SENSOR_TYPE","LOCATION_TYPE")))
                .andExpect(jsonPath("$.data[*].groupName").value(hasItems("장비 유형", "센서 유형", "위치 유형")));
    }

    private void createCodeGroup(String accessToken, String groupKey, String groupName) throws Exception {
        mockMvc.perform(post("/api/manager/code-groups")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"groupKey": "%s", "groupName": "%s"}
                                """.formatted(groupKey, groupName)))
                .andExpect(status().isOk());
    }

    @Test
    void createCodeGroup_returnUnauthorizedForWrongAccount() throws Exception {
        mockMvc.perform(post("/api/manager/code-groups")
                        .header("Authorization", bearerToken("invalid.access.token"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"groupKey": "Test_Type", "groupName": "TEST"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void createCodeGroup_returnEmptyCodeGroupRequest() throws Exception{
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "TEST", "TEST");
        mockMvc.perform(post("/api/manager/code-groups")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"groupKey": "", "groupName": ""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createCodeGroup_returnWrongUrl() throws Exception{
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "TEST", "TEST");
        mockMvc.perform(post("/unKnown/url")
                .header("Authorization", bearerToken(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"groupKey": "TEST_Type", "groupName": "TEST"}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
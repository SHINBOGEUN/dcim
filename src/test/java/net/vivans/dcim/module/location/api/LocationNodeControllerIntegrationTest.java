package net.vivans.dcim.module.location.api;

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
import org.springframework.transaction.annotation.Transactional;

import static net.vivans.dcim.support.AuthTestSupport.bearerToken;
import static net.vivans.dcim.support.AuthTestSupport.loginAndGetAccessToken;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ManagerServerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class LocationNodeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndGetTree_returnsNestedChildren() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "location-tree-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "LOCATION_TYPE", "위치 유형");
        Integer containerTypeId = createCommonCode(accessToken, groupId, "CONTAINER", "컨테이너", 1);
        Integer rowTypeId = createCommonCode(accessToken, groupId, "ROW", "열", 2);

        String rootCode = createLocationNode(accessToken, null, containerTypeId, "컨테이너 A");
        createLocationNode(accessToken, rootCode, rowTypeId, "A열");

        mockMvc.perform(get("/api/manager/location-node")
                        .header("Authorization", bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].code").value(rootCode))
                .andExpect(jsonPath("$.data[0].name").value("컨테이너 A"))
                .andExpect(jsonPath("$.data[0].children", hasSize(1)))
                .andExpect(jsonPath("$.data[0].children[0].parentCode").value(rootCode))
                .andExpect(jsonPath("$.data[0].children[0].name").value("A열"));
    }

    @Test
    void update_updatesNameAndLocationType() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "location-update-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "LOCATION_TYPE", "위치 유형");
        Integer containerTypeId = createCommonCode(accessToken, groupId, "CONTAINER", "컨테이너", 1);
        Integer zoneTypeId = createCommonCode(accessToken, groupId, "ZONE", "존", 2);

        String rootCode = createLocationNode(accessToken, null, containerTypeId, "컨테이너 A");

        mockMvc.perform(put("/api/manager/location-node/{code}", rootCode)
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"locationTypeId": %d, "name": "컨테이너 A (수정)"}
                                """.formatted(zoneTypeId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.code").value(rootCode))
                .andExpect(jsonPath("$.data.name").value("컨테이너 A (수정)"))
                .andExpect(jsonPath("$.data.locationTypeId").value(zoneTypeId));
    }

    @Test
    void updateParent_promotesChildToRoot() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "location-parent-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "LOCATION_TYPE", "위치 유형");
        Integer containerTypeId = createCommonCode(accessToken, groupId, "CONTAINER", "컨테이너", 1);
        Integer rowTypeId = createCommonCode(accessToken, groupId, "ROW", "열", 2);

        String rootCode = createLocationNode(accessToken, null, containerTypeId, "컨테이너 A");
        String rowCode = createLocationNode(accessToken, rootCode, rowTypeId, "A열");

        mockMvc.perform(patch("/api/manager/location-node/{code}/parent", rowCode)
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parentCode": null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.code").value(rowCode))
                .andExpect(jsonPath("$.data.parentCode").value(nullValue()));
    }

    @Test
    void create_duplicateNameUnderSameParent_returnsBadRequest() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "location-dup-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "LOCATION_TYPE", "위치 유형");
        Integer containerTypeId = createCommonCode(accessToken, groupId, "CONTAINER", "컨테이너", 1);

        createLocationNode(accessToken, null, containerTypeId, "컨테이너 A");

        mockMvc.perform(post("/api/manager/location-node")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parentCode": null, "locationTypeId": %d, "name": "컨테이너 A"}
                                """.formatted(containerTypeId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void get_withUnknownParentCode_returnsNotFound() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "location-notfound-user", "password123");

        mockMvc.perform(get("/api/manager/location-node")
                        .param("parentCode", "UNKNOWN01")
                        .header("Authorization", bearerToken(accessToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private Integer createCodeGroup(String accessToken, String groupKey, String groupName) throws Exception {
        String response = mockMvc.perform(post("/api/manager/code-groups")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"groupKey": "%s", "groupName": "%s"}
                                """.formatted(groupKey, groupName)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

    private Integer createCommonCode(
            String accessToken,
            Integer groupId,
            String code,
            String name,
            Integer sortOrder
    ) throws Exception {
        String response = mockMvc.perform(post("/api/manager/common-codes")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"groupId": %d, "code": "%s", "name": "%s", "sortOrder": %d}
                                """.formatted(groupId, code, name, sortOrder)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

    private String createLocationNode(
            String accessToken,
            String parentCode,
            Integer locationTypeId,
            String name
    ) throws Exception {
        String parentJson = parentCode == null ? "null" : "\"%s\"".formatted(parentCode);
        String response = mockMvc.perform(post("/api/manager/location-node")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"parentCode": %s, "locationTypeId": %d, "name": "%s"}
                                """.formatted(parentJson, locationTypeId, name)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode codeNode = objectMapper.readTree(response).path("data").path("code");
        return codeNode.asText();
    }
}

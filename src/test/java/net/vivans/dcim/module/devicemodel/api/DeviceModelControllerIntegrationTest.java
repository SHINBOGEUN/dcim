package net.vivans.dcim.module.devicemodel.api;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ManagerServerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class DeviceModelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createAndGetDeviceModel_returnsProtocols() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "device-model-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "PROTOCOL_TYPE", "Protocol Type");
        Integer mqttId = createCommonCode(accessToken, groupId, "mqtt", "MQTT", 1);

        mockMvc.perform(post("/api/manager/device-models")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "LHT65N-PIR",
                                  "manufacturer": "Dragino",
                                  "description": "동작 감지 센서",
                                  "protocols": [
                                    { "protocolTypeId": %d, "isDefault": true, "sortOrder": 1 }
                                  ]
                                }
                                """.formatted(mqttId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("LHT65N-PIR"))
                .andExpect(jsonPath("$.data.protocols", hasSize(1)))
                .andExpect(jsonPath("$.data.protocols[0].protocolCode").value("mqtt"));

        mockMvc.perform(get("/api/manager/device-models")
                        .header("Authorization", bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].manufacturer").value("Dragino"));
    }

    @Test
    void createDuplicateModel_returnsBadRequest() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "device-model-dup-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "PROTOCOL_TYPE", "Protocol Type");
        Integer mqttId = createCommonCode(accessToken, groupId, "mqtt", "MQTT", 1);

        String body = """
                {
                  "name": "LHT65N-PIR",
                  "manufacturer": "Dragino",
                  "protocols": [
                    { "protocolTypeId": %d }
                  ]
                }
                """.formatted(mqttId);

        mockMvc.perform(post("/api/manager/device-models")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/manager/device-models")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("device model already exists"));
    }

    @Test
    void updateDeviceModel_replacesProtocols() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "device-model-update-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "PROTOCOL_TYPE", "Protocol Type");
        Integer mqttId = createCommonCode(accessToken, groupId, "mqtt", "MQTT", 1);
        Integer modbusId = createCommonCode(accessToken, groupId, "modbus", "Modbus", 2);

        String createResponse = mockMvc.perform(post("/api/manager/device-models")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "LHT65N-PIR",
                                  "manufacturer": "Dragino",
                                  "protocols": [
                                    { "protocolTypeId": %d, "isDefault": true }
                                  ]
                                }
                                """.formatted(mqttId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int modelId = objectMapper.readTree(createResponse).path("data").path("id").asInt();

        mockMvc.perform(put("/api/manager/device-models/{id}", modelId)
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "LHT65N-PIR",
                                  "manufacturer": "Dragino",
                                  "description": "updated",
                                  "protocols": [
                                    { "protocolTypeId": %d, "isDefault": false, "sortOrder": 2 },
                                    { "protocolTypeId": %d, "isDefault": true, "sortOrder": 1 }
                                  ]
                                }
                                """.formatted(modbusId, mqttId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.description").value("updated"))
                .andExpect(jsonPath("$.data.protocols", hasSize(2)))
                .andExpect(jsonPath("$.data.protocols[0].protocolCode").value("mqtt"))
                .andExpect(jsonPath("$.data.protocols[0].isDefault").value(true));
    }

    @Test
    void deleteDeviceModel_removesModel() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "device-model-delete-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "PROTOCOL_TYPE", "Protocol Type");
        Integer mqttId = createCommonCode(accessToken, groupId, "mqtt", "MQTT", 1);

        String createResponse = mockMvc.perform(post("/api/manager/device-models")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "LHT65N-PIR",
                                  "manufacturer": "Dragino",
                                  "protocols": [
                                    { "protocolTypeId": %d, "isDefault": true }
                                  ]
                                }
                                """.formatted(mqttId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int modelId = objectMapper.readTree(createResponse).path("data").path("id").asInt();

        mockMvc.perform(delete("/api/manager/device-models/{id}", modelId)
                        .header("Authorization", bearerToken(accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(modelId));

        mockMvc.perform(get("/api/manager/device-models/{id}", modelId)
                        .header("Authorization", bearerToken(accessToken)))
                .andExpect(status().isNotFound());
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
}

package net.vivans.dcim.module.device.api;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ManagerServerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class DeviceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createDevice_returnsCreatedDevice() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "device-create-user", "password123");
        Integer modelId = createDeviceModel(accessToken, "AP8959", "APC");
        String locationCode = createRootLocation(accessToken, "Rack-01");

        mockMvc.perform(post("/api/manager/devices")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "modelId": %d,
                                  "locationNodeCode": "%s",
                                  "name": "PDU-좌",
                                  "description": "Rack-01 좌측 PDU",
                                  "enabled": true
                                }
                                """.formatted(modelId, locationCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.modelId").value(modelId))
                .andExpect(jsonPath("$.data.modelName").value("AP8959"))
                .andExpect(jsonPath("$.data.manufacturer").value("APC"))
                .andExpect(jsonPath("$.data.locationNodeCode").value(locationCode))
                .andExpect(jsonPath("$.data.name").value("PDU-좌"))
                .andExpect(jsonPath("$.data.description").value("Rack-01 좌측 PDU"))
                .andExpect(jsonPath("$.data.enabled").value(true));
    }

    @Test
    void createDevice_withoutEnabled_defaultsToTrue() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "device-create-default-enabled", "password123");
        Integer modelId = createDeviceModel(accessToken, "LHT65N", "Dragino");
        String locationCode = createRootLocation(accessToken, "Zone-A");

        mockMvc.perform(post("/api/manager/devices")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "modelId": %d,
                                  "locationNodeCode": "%s",
                                  "name": "센서-01"
                                }
                                """.formatted(modelId, locationCode)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.description").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void createDevice_whenModelNotFound_returnsNotFound() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "device-create-model-nf", "password123");
        String locationCode = createRootLocation(accessToken, "Rack-NF");

        mockMvc.perform(post("/api/manager/devices")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "modelId": 999999,
                                  "locationNodeCode": "%s",
                                  "name": "PDU-좌"
                                }
                                """.formatted(locationCode)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("DeviceModel not found: 999999"));
    }

    @Test
    void createDevice_whenLocationNotFound_returnsNotFound() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "device-create-loc-nf", "password123");
        Integer modelId = createDeviceModel(accessToken, "AP8959", "APC");

        mockMvc.perform(post("/api/manager/devices")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "modelId": %d,
                                  "locationNodeCode": "UNKNOWN01",
                                  "name": "PDU-좌"
                                }
                                """.formatted(modelId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("LocationNode not found: UNKNOWN01"));
    }

    @Test
    void createDevice_withDuplicateNameAtSameLocation_returnsBadRequest() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "device-create-dup", "password123");
        Integer modelId = createDeviceModel(accessToken, "AP8959", "APC");
        String locationCode = createRootLocation(accessToken, "Rack-Dup");

        String body = """
                {
                  "modelId": %d,
                  "locationNodeCode": "%s",
                  "name": "PDU-좌"
                }
                """.formatted(modelId, locationCode);

        mockMvc.perform(post("/api/manager/devices")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/manager/devices")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("device name already exists at this location"));
    }

    @Test
    void createDevice_withBlankName_returnsBadRequest() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "device-create-blank-name", "password123");
        Integer modelId = createDeviceModel(accessToken, "AP8959", "APC");
        String locationCode = createRootLocation(accessToken, "Rack-Blank");

        mockMvc.perform(post("/api/manager/devices")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "modelId": %d,
                                  "locationNodeCode": "%s",
                                  "name": " "
                                }
                                """.formatted(modelId, locationCode)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for parameter 'name'"));
    }

    @Test
    void createDevice_withoutModelId_returnsBadRequest() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "device-create-null-model", "password123");
        String locationCode = createRootLocation(accessToken, "Rack-NullModel");

        mockMvc.perform(post("/api/manager/devices")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "locationNodeCode": "%s",
                                  "name": "PDU-좌"
                                }
                                """.formatted(locationCode)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for parameter 'modelId'"));
    }

    private Integer createDeviceModel(String accessToken, String name, String manufacturer) throws Exception {
        Integer groupId = createCodeGroup(accessToken, "PROTOCOL_TYPE", "Protocol Type");
        Integer snmpId = createCommonCode(accessToken, groupId, "snmp", "SNMP", 1);

        String response = mockMvc.perform(post("/api/manager/device-models")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "manufacturer": "%s",
                                  "protocols": [
                                    { "protocolTypeId": %d }
                                  ]
                                }
                                """.formatted(name, manufacturer, snmpId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).path("data").path("id").asInt();
    }

    private String createRootLocation(String accessToken, String name) throws Exception {
        Integer groupId = createCodeGroup(accessToken, "LOCATION_TYPE", "Location Type");
        Integer rackTypeId = createCommonCode(accessToken, groupId, "RACK", "랙", 3);
        return createLocationNode(accessToken, null, rackTypeId, name);
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

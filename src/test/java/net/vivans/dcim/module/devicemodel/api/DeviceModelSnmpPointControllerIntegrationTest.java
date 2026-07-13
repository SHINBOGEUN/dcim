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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ManagerServerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class DeviceModelSnmpPointControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createSnmpPoint_returnsCreated() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "snmp-point-create-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "PROTOCOL_TYPE", "Protocol Type");
        Integer snmpId = createCommonCode(accessToken, groupId, "snmp", "SNMP", 1);

        String modelResponse = mockMvc.perform(post("/api/manager/device-models")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "CDU-100",
                                  "manufacturer": "Vivans",
                                  "protocols": [
                                    { "protocolTypeId": %d }
                                  ]
                                }
                                """.formatted(snmpId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int modelId = objectMapper.readTree(modelResponse).path("data").path("id").asInt();
        int protocolId = objectMapper.readTree(modelResponse).path("data").path("protocols").get(0).path("id").asInt();

        mockMvc.perform(post("/api/manager/device-models/{modelId}/protocols/{protocolId}/snmp-points", modelId, protocolId)
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "PRI-FLOW",
                                  "oid": "1.3.6.1.4.1.12345.{instanceId}.10.1.0",
                                  "requiresInstance": true,
                                  "unit": "L/min",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("PRI-FLOW"))
                .andExpect(jsonPath("$.data.modelId").value(modelId))
                .andExpect(jsonPath("$.data.protocolId").value(protocolId))
                .andExpect(jsonPath("$.data.requiresInstance").value(true))
                .andExpect(jsonPath("$.data.unit").value("L/min"));
    }

    @Test
    void updateSnmpPoint_returnsUpdated() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "snmp-point-update-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "PROTOCOL_TYPE", "Protocol Type");
        Integer snmpId = createCommonCode(accessToken, groupId, "snmp", "SNMP", 1);

        String modelResponse = mockMvc.perform(post("/api/manager/device-models")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "CDU-200",
                                  "manufacturer": "Vivans",
                                  "protocols": [
                                    { "protocolTypeId": %d }
                                  ]
                                }
                                """.formatted(snmpId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int modelId = objectMapper.readTree(modelResponse).path("data").path("id").asInt();
        int protocolId = objectMapper.readTree(modelResponse).path("data").path("protocols").get(0).path("id").asInt();

        String createResponse = mockMvc.perform(post(
                        "/api/manager/device-models/{modelId}/protocols/{protocolId}/snmp-points",
                        modelId, protocolId)
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "PRI-FLOW",
                                  "oid": "1.3.6.1.4.1.12345.{instanceId}.10.1.0",
                                  "requiresInstance": true,
                                  "unit": "L/min",
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int pointId = objectMapper.readTree(createResponse).path("data").path("id").asInt();

        mockMvc.perform(put(
                        "/api/manager/device-models/{modelId}/protocols/{protocolId}/snmp-points/{pointId}",
                        modelId, protocolId, pointId)
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "SEC-FLOW",
                                  "oid": "1.3.6.1.4.1.12345.10.2.0",
                                  "requiresInstance": false,
                                  "unit": "L/min",
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(pointId))
                .andExpect(jsonPath("$.data.name").value("SEC-FLOW"))
                .andExpect(jsonPath("$.data.oid").value("1.3.6.1.4.1.12345.10.2.0"))
                .andExpect(jsonPath("$.data.requiresInstance").value(false))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    void updateSnmpPoint_whenNotFound_returnsNotFound() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "snmp-point-update-nf-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "PROTOCOL_TYPE", "Protocol Type");
        Integer snmpId = createCommonCode(accessToken, groupId, "snmp", "SNMP", 1);

        String modelResponse = mockMvc.perform(post("/api/manager/device-models")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "CDU-300",
                                  "manufacturer": "Vivans",
                                  "protocols": [
                                    { "protocolTypeId": %d }
                                  ]
                                }
                                """.formatted(snmpId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int modelId = objectMapper.readTree(modelResponse).path("data").path("id").asInt();
        int protocolId = objectMapper.readTree(modelResponse).path("data").path("protocols").get(0).path("id").asInt();

        mockMvc.perform(put(
                        "/api/manager/device-models/{modelId}/protocols/{protocolId}/snmp-points/{pointId}",
                        modelId, protocolId, 999999)
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "SEC-FLOW",
                                  "oid": "1.3.6.1.4.1.12345.10.2.0"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("DeviceModelSnmpPoint not found: 999999"));
    }

    @Test
    void createSnmpPoint_whenProtocolIsNotSnmp_returnsBadRequest() throws Exception {
        String accessToken = loginAndGetAccessToken(mockMvc, objectMapper, "snmp-point-mqtt-user", "password123");
        Integer groupId = createCodeGroup(accessToken, "PROTOCOL_TYPE", "Protocol Type");
        Integer mqttId = createCommonCode(accessToken, groupId, "mqtt", "MQTT", 1);

        String modelResponse = mockMvc.perform(post("/api/manager/device-models")
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "LHT65N-PIR",
                                  "manufacturer": "Dragino",
                                  "protocols": [
                                    { "protocolTypeId": %d }
                                  ]
                                }
                                """.formatted(mqttId)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int modelId = objectMapper.readTree(modelResponse).path("data").path("id").asInt();
        int protocolId = objectMapper.readTree(modelResponse).path("data").path("protocols").get(0).path("id").asInt();

        mockMvc.perform(post("/api/manager/device-models/{modelId}/protocols/{protocolId}/snmp-points", modelId, protocolId)
                        .header("Authorization", bearerToken(accessToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "V",
                                  "oid": "1.3.6.1.4.1.12345.10.1.0"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("protocol must be snmp"));
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

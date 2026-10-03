package net.vivans.dcim.module.device.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.vivans.dcim.bootstrap.ManagerServerApplication;
import net.vivans.dcim.module.common.domain.model.CodeGroup;
import net.vivans.dcim.module.common.domain.model.CommonCode;
import net.vivans.dcim.module.common.domain.repository.CodeGroupRepository;
import net.vivans.dcim.module.common.domain.repository.CommonCodeRepository;
import net.vivans.dcim.module.device.domain.model.Device;
import net.vivans.dcim.module.device.domain.repository.DeviceRepository;
import net.vivans.dcim.module.devicemodel.domain.model.DeviceModel;
import net.vivans.dcim.module.devicemodel.domain.repository.DeviceModelRepository;
import net.vivans.dcim.module.identity.domain.model.UserRole;
import net.vivans.dcim.module.identity.domain.repository.UserRepository;
import net.vivans.dcim.module.location.domain.model.LocationNode;
import net.vivans.dcim.module.location.domain.repository.LocationNodeRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ManagerServerApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class DevicePageMeasurementControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired CodeGroupRepository codeGroupRepository;
    @Autowired CommonCodeRepository commonCodeRepository;
    @Autowired DeviceModelRepository modelRepository;
    @Autowired LocationNodeRepository locationRepository;
    @Autowired DeviceRepository deviceRepository;

    @Test
    void selectedDevicesAppearWithEmptyPointListAndCanBeCleared() throws Exception {
        String admin = loginAndGetAccessToken(mockMvc, objectMapper, userRepository,
                "device-page-admin", "password123");
        CommonCode page = code("DEVICE_PAGE", "COOLING", "Cooling");
        CommonCode modelType = code("MODEL_TYPE", "COOLER", "Cooler");
        CommonCode locationType = code("LOCATION_TYPE", "ROOM", "Room");
        DeviceModel model = modelRepository.save(DeviceModel.create("Page Cooler", "Test", modelType, null));
        LocationNode location = locationRepository.save(LocationNode.createRoot("PAGEROOM01", locationType, "Page Room"));
        Device device = deviceRepository.save(Device.create(model, location, "Cooler 1", null));

        mockMvc.perform(put("/api/manager/device-pages/{pageCode}/devices", page.getCode())
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceIds\":[%d]}".formatted(device.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value(device.getId()));

        mockMvc.perform(get("/api/manager/device-pages/{pageCode}/measurements", page.getCode())
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pageCode").value("COOLING"))
                .andExpect(jsonPath("$.data.devices", hasSize(1)))
                .andExpect(jsonPath("$.data.devices[0].deviceId").value(device.getId()))
                .andExpect(jsonPath("$.data.devices[0].points", hasSize(0)));

        mockMvc.perform(put("/api/manager/device-pages/COOLING/devices")
                        .header("Authorization", bearerToken(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceIds\":[]}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/manager/device-pages/COOLING/measurements")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.devices", hasSize(0)));
    }

    @Test
    void rejectsMissingPageAndUserMutation() throws Exception {
        String admin = loginAndGetAccessToken(mockMvc, objectMapper, userRepository,
                "device-page-admin-2", "password123");
        String user = loginAndGetAccessToken(mockMvc, objectMapper, userRepository,
                "device-page-user", "password123", UserRole.USER);
        code("DEVICE_PAGE", "COOLING", "Cooling");

        mockMvc.perform(put("/api/manager/device-pages/COOLING/devices")
                        .header("Authorization", bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deviceIds\":[]}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/manager/device-pages/MISSING/measurements")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/manager/device-pages/COOLING/measurements")
                        .param("lookbackHours", "721")
                        .header("Authorization", bearerToken(admin)))
                .andExpect(status().isBadRequest());
    }

    private CommonCode code(String groupKey, String value, String name) {
        CodeGroup group = codeGroupRepository.findAll().stream()
                .filter(item -> groupKey.equals(item.getGroupKey())).findFirst()
                .orElseGet(() -> codeGroupRepository.save(CodeGroup.createCodeGroup(groupKey, groupKey)));
        return commonCodeRepository.findByCodeGroupGroupKeyAndCode(groupKey, value)
                .orElseGet(() -> commonCodeRepository.save(CommonCode.createCommonCode(group, value, name, 1)));
    }
}

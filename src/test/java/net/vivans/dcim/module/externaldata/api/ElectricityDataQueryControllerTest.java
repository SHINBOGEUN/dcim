package net.vivans.dcim.module.externaldata.api;

import net.vivans.dcim.module.externaldata.application.ElectricityDataQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ElectricityDataQueryControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ElectricityDataQueryController(mock(ElectricityDataQueryService.class))).build();
    }

    @Test
    void existingBusinessQueriesRemainAvailable() throws Exception {
        mockMvc.perform(get("/api/manager/electricity-bill/monthly"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/manager/electricity-bill/hourly/latest"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/manager/power-usage/daily/yesterday"))
                .andExpect(status().isOk());
    }

    @Test
    void oldWriteRoutesAreNoLongerAvailable() throws Exception {
        mockMvc.perform(post("/api/manager/electricity-bill/monthly"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/api/manager/electricity-bill/hourly"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(post("/api/manager/power-usage/daily"))
                .andExpect(status().isMethodNotAllowed());
    }
}

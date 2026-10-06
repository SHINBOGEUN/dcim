package net.vivans.dcim.module.externaldata.api;

import net.vivans.dcim.module.externaldata.application.ExternalDataService;
import net.vivans.dcim.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExternalDataControllerTest {

    private ExternalDataService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(ExternalDataService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ExternalDataController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/internal/external-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataCategory\":\"BILLING\",\"periodType\":\"MONTHLY\",\"payload\": {"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void blankDataCategoryReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/internal/external-data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataCategory\":\" \",\"periodType\":\"MONTHLY\",\"payload\":{}}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}

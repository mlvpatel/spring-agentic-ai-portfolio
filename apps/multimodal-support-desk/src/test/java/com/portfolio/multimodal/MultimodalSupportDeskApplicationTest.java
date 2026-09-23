package com.portfolio.multimodal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MultimodalSupportDeskApplicationTest {

    private static final String KEY = "test-multimodal-desk-api-key-for-unit-tests-only";
    private static final String BODY = """
            {"ticket":"Payment outage in checkout","imageMeta":"png 800x600"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Context loads")
    void contextLoads() {
    }

    @Test
    @DisplayName("Health is UP without skill brands")
    void health() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Requires API key")
    void unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/triage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rejects ey* Bearer bypass")
    void rejectsEy() throws Exception {
        mockMvc.perform(post("/api/v1/triage")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake.sig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rejects valid-test-token bypass")
    void rejectsValidTestToken() throws Exception {
        mockMvc.perform(post("/api/v1/triage")
                        .header("Authorization", "Bearer valid-test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Primary flow offline")
    void primary() throws Exception {
        mockMvc.perform(post("/api/v1/triage")
                        .header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("offline"))
                .andExpect(jsonPath("$.severity").value("high"));
    }
}

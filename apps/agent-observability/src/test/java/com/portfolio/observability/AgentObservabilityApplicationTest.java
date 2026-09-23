package com.portfolio.observability;

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
class AgentObservabilityApplicationTest {
    private static final String KEY = "test-agent-observability-api-key-for-unit-tests-only";

    @Autowired
    MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void health() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(r -> org.assertj.core.api.Assertions.assertThat(r.getResponse().getContentAsString())
                        .doesNotContainIgnoringCase("ponytail")
                        .doesNotContainIgnoringCase("plugin"));
    }

    @Test
    void unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/trajectories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"p\",\"tool\":\"t\",\"outcome\":\"ok\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsEy() throws Exception {
        mockMvc.perform(post("/api/v1/trajectories")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.x.y")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"p\",\"tool\":\"t\",\"outcome\":\"ok\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsValidTestToken() throws Exception {
        mockMvc.perform(post("/api/v1/trajectories")
                        .header("Authorization", "Bearer valid-test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"p\",\"tool\":\"t\",\"outcome\":\"ok\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recordAndAnalyze() throws Exception {
        mockMvc.perform(post("/api/v1/trajectories").header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"prompt\":\"call search\",\"tool\":\"search\",\"outcome\":\"error timeout\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.failed").value(true));
        mockMvc.perform(post("/api/v1/analyze").header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("offline"));
    }

}

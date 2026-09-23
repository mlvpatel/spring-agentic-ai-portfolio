package com.portfolio.specorch;

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
class SpecOrchestratorApplicationTest {
    private static final String KEY = "test-spec-orchestrator-api-key-for-unit-tests-only";

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
        mockMvc.perform(post("/api/v1/runs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"brief\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsEy() throws Exception {
        mockMvc.perform(post("/api/v1/runs")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.x.y")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"brief\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsValidTestToken() throws Exception {
        mockMvc.perform(post("/api/v1/runs")
                        .header("Authorization", "Bearer valid-test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"brief\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createAndGet() throws Exception {
        String body = mockMvc.perform(post("/api/v1/runs").header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"brief\":\"Add refund API\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.mode").value("offline"))
                .andReturn().getResponse().getContentAsString();
        String id = com.jayway.jsonpath.JsonPath.read(body, "$.id");
        mockMvc.perform(get("/api/v1/runs/" + id).header("X-API-Key", KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

}

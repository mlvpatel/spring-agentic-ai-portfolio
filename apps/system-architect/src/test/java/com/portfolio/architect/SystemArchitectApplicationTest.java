package com.portfolio.architect;

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
class SystemArchitectApplicationTest {
    private static final String KEY = "test-system-architect-api-key-for-unit-tests-only";

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
        mockMvc.perform(post("/api/v1/architect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requirements\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsEy() throws Exception {
        mockMvc.perform(post("/api/v1/architect")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.x.y")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requirements\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsValidTestToken() throws Exception {
        mockMvc.perform(post("/api/v1/architect")
                        .header("Authorization", "Bearer valid-test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requirements\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void architectOk() throws Exception {
        mockMvc.perform(post("/api/v1/architect").header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"requirements\":\"Build checkout API for EU retail\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("offline"))
                .andExpect(jsonPath("$.adr.title").exists())
                .andExpect(jsonPath("$.c4Mermaid").exists());
    }

}

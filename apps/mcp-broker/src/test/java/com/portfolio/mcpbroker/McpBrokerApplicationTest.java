package com.portfolio.mcpbroker;

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
class McpBrokerApplicationTest {
    private static final String KEY = "test-mcp-broker-api-key-for-unit-tests-only";

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
        mockMvc.perform(post("/api/v1/tools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"scope\":\"read\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsEy() throws Exception {
        mockMvc.perform(post("/api/v1/tools")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.x.y")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"scope\":\"read\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsValidTestToken() throws Exception {
        mockMvc.perform(post("/api/v1/tools")
                        .header("Authorization", "Bearer valid-test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\",\"scope\":\"read\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerAndInvoke() throws Exception {
        mockMvc.perform(post("/api/v1/tools").header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"echo\",\"scope\":\"read\",\"schema\":{\"type\":\"object\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("echo"));
        mockMvc.perform(post("/api/v1/invoke").header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"echo\",\"args\":{\"q\":\"hi\"}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(true));
    }

}

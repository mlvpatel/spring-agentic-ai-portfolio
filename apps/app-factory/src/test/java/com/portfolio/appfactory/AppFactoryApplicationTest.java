package com.portfolio.appfactory;

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
class AppFactoryApplicationTest {
    private static final String KEY = "test-app-factory-api-key-for-unit-tests-only";

    @Autowired
    MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void health() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(r -> org.assertj.core.api.Assertions.assertThat(r.getResponse().getContentAsString())
                        .doesNotContainIgnoringCase("plugin"));
    }

    @Test
    void unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsEy() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints").header("Authorization", "Bearer eyJ.x.y"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsValidTestToken() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints").header("Authorization", "Bearer valid-test-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listAndGenerate() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints").header("X-API-Key", KEY)).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/generate").header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"blueprint\":\"crud-api\",\"appName\":\"demo-api\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("offline"))
                .andExpect(jsonPath("$.files['pom.xml']").exists());
    }
}

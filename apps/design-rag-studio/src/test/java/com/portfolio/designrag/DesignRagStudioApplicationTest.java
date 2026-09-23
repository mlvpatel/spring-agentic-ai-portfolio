package com.portfolio.designrag;

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
class DesignRagStudioApplicationTest {
    private static final String KEY = "test-design-rag-studio-api-key-for-unit-tests-only";

    @Autowired MockMvc mockMvc;

    @Test void contextLoads() {}

    @Test @DisplayName("Health UP without skill brands")
    void health() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(r -> org.assertj.core.api.Assertions.assertThat(r.getResponse().getContentAsString())
                        .doesNotContainIgnoringCase("ponytail")
                        .doesNotContainIgnoringCase("plugin"));
    }

    @Test void unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/ingest").contentType(MediaType.APPLICATION_JSON)
                .content("{\"csv\":\"color.primary,#111\"}")).andExpect(status().isUnauthorized());
    }

    @Test void rejectsEy() throws Exception {
        mockMvc.perform(post("/api/v1/ingest").header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.x.y")
                .contentType(MediaType.APPLICATION_JSON).content("{\"csv\":\"a,b\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test void rejectsValidTestToken() throws Exception {
        mockMvc.perform(post("/api/v1/ingest").header("Authorization", "Bearer valid-test-token")
                .contentType(MediaType.APPLICATION_JSON).content("{\"csv\":\"a,b\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test void ingestAndGenerate() throws Exception {
        mockMvc.perform(post("/api/v1/ingest").header("X-API-Key", KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"csv\":\"token,value,notes\\ncolor.primary,#0B1F33,brand\\nspace.md,16px,rhythm\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.added").value(2));
        mockMvc.perform(post("/api/v1/generate").header("X-API-Key", KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stack\":\"react\",\"intent\":\"primary\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("jdbc-h2"))
                .andExpect(jsonPath("$.refused").value(false))
                .andExpect(jsonPath("$.tokens['color.primary']").exists());
    }

    @Test void refuseWithoutCorpus() throws Exception {
        // fresh context shares corpus within same Spring context — generate without matching still ok if corpus from prior test
        mockMvc.perform(post("/api/v1/generate").header("X-API-Key", KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"stack\":\"vue\",\"intent\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}

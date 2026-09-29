package com.portfolio.designrag;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {
        "design.rag.vector-retrieval=true",
        "app.security.api-key=test-design-rag-studio-api-key-for-unit-tests-only",
        "spring.datasource.url=jdbc:h2:mem:designrag_vector;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
class VectorRetrievalApplicationTest {
    private static final String KEY = "test-design-rag-studio-api-key-for-unit-tests-only";

    @Autowired MockMvc mockMvc;

    @Test
    @Order(1)
    @DisplayName("Empty corpus refuses on vector path")
    void emptyCorpusRefuses() throws Exception {
        mockMvc.perform(post("/api/v1/generate").header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stack\":\"react\",\"intent\":\"primary brand\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refused").value(true))
                .andExpect(jsonPath("$.mode").value("vector-pgvector"));
    }

    @Test
    @Order(2)
    @DisplayName("Ingested snippet is returned for matching query on vector path")
    void matchingSnippetReturned() throws Exception {
        mockMvc.perform(post("/api/v1/ingest").header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"csv\":\"token,value,notes\\ncolor.primary,#0B1F33,brand navy\\nspace.md,16px,rhythm\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.added").value(2));

        mockMvc.perform(post("/api/v1/generate").header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stack\":\"react\",\"intent\":\"primary brand navy\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refused").value(false))
                .andExpect(jsonPath("$.mode").value("vector-pgvector"))
                .andExpect(jsonPath("$.tokens['color.primary']").exists());
    }
}

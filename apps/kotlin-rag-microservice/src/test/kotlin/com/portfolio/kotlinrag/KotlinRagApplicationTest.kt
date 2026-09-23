package com.portfolio.kotlinrag

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class KotlinRagApplicationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val corpus: com.portfolio.kotlinrag.service.CorpusService,
) {

    private val key = "test-kotlin-rag-api-key-for-unit-tests-only"

    @Test
    @DisplayName("Context loads")
    fun contextLoads() {
    }

    @Test
    @DisplayName("Health is UP")
    fun health() {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("UP"))
    }

    @Test
    @DisplayName("Requires API key")
    fun unauthorized() {
        mockMvc.perform(
            post("/api/v1/ingest")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"t","text":"x"}""")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("Rejects ey* Bearer bypass")
    fun rejectsEy() {
        mockMvc.perform(
            post("/api/v1/ingest")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake.sig")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"t","text":"x"}""")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("Empty corpus query refuses")
    fun emptyCorpusRefuses() {
        corpus.clear()
        mockMvc.perform(
            post("/api/v1/query")
                .header("X-API-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"q":"vector"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.refused").value(true))
            .andExpect(jsonPath("$.hits").isArray)
    }

    @Test
    @DisplayName("Ingest and query offline")
    fun ingestAndQuery() {
        corpus.clear()
        mockMvc.perform(
            post("/api/v1/ingest")
                .header("X-API-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"title":"RAG notes","text":"vector retrieval with citations"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mode").value("offline"))

        mockMvc.perform(
            post("/api/v1/query")
                .header("X-API-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"q":"vector"}""")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.mode").value("offline"))
            .andExpect(jsonPath("$.hits").isArray)
            .andExpect(jsonPath("$.refused").value(false))
    }
}

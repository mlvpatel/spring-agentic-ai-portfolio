package com.portfolio.designrag;

import com.portfolio.designrag.service.JdbcDesignCorpus;
import com.portfolio.designrag.service.TokenDoc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("pgvector")
@Testcontainers(disabledWithoutDocker = true)
class JdbcDesignCorpusIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("design")
            .withUsername("postgres")
            .withPassword("design");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.security.api-key", () -> "test-design-rag-studio-api-key-for-unit-tests-only");
    }

    @Autowired
    private JdbcDesignCorpus corpus;

    @Test
    @DisplayName("Postgres-backed corpus ingest and retrieve without LLM key")
    void jdbcRoundTrip() {
        int added = corpus.ingestCsv("token,value,notes\nprimary,#112233,brand\n");
        assertThat(added).isEqualTo(1);
        assertThat(corpus.size()).isGreaterThanOrEqualTo(1);
        List<TokenDoc> hits = corpus.retrieve("primary", 5);
        assertThat(hits).isNotEmpty();
        assertThat(hits.get(0).name()).isEqualTo("primary");
    }
}

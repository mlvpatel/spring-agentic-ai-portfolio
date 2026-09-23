package com.portfolio.paperlab.service;

import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PaperLabService {
    private final JdbcTemplate jdbc;
    private final Environment environment;

    public PaperLabService(JdbcTemplate jdbc, Environment environment) {
        this.jdbc = jdbc;
        this.environment = environment;
    }

    @PostConstruct
    void schema() {
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS papers (
                  id VARCHAR(64) PRIMARY KEY,
                  title VARCHAR(512) NOT NULL,
                  body_text VARCHAR(8000) NOT NULL
                )
                """);
    }

    public Map<String, Object> ingest(String title, String text) {
        if (title == null || title.isBlank() || text == null || text.isBlank()) {
            throw new IllegalArgumentException("title and text required");
        }
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM papers", Integer.class);
        String id = "paper-" + ((count == null ? 0 : count) + 1);
        jdbc.update("INSERT INTO papers(id, title, body_text) VALUES (?,?,?)", id, title.strip(), text.strip());
        return Map.of("mode", mode(), "id", id, "title", title.strip(), "chars", text.length());
    }

    public Map<String, Object> synthesize(String topic) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("topic required");
        }
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM papers", Integer.class);
        if (count == null || count == 0) {
            throw new IllegalArgumentException("ingest papers first");
        }
        String q = "%" + topic.toLowerCase(Locale.ROOT) + "%";
        List<Map<String, String>> cites = jdbc.query(
                """
                SELECT id, title, body_text FROM papers
                WHERE LOWER(title) LIKE ? OR LOWER(body_text) LIKE ?
                LIMIT 3
                """,
                (rs, i) -> Map.of(
                        "id", rs.getString("id"),
                        "title", rs.getString("title"),
                        "text", rs.getString("body_text")),
                q, q);
        if (cites.isEmpty()) {
            cites = jdbc.query(
                    "SELECT id, title, body_text FROM papers LIMIT 1",
                    (rs, i) -> Map.of(
                            "id", rs.getString("id"),
                            "title", rs.getString("title"),
                            "text", rs.getString("body_text")));
        }
        String javaCode = "// Synthesized offline prototype for: " + topic.strip()
                + "\n// Citation: " + cites.get(0).get("title")
                + "\npublic final class AlgorithmPrototype {\n"
                + "  private AlgorithmPrototype() {}\n"
                + "  public static int step(int x) { return x; }\n"
                + "}\n";
        String testCode = "// assert AlgorithmPrototype.step(1) == 1;\n";
        List<Map<String, String>> citationList = cites.stream()
                .map(c -> Map.of("id", c.get("id"), "title", c.get("title")))
                .toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("mode", mode());
        out.put("topic", topic.strip());
        out.put("java", javaCode);
        out.put("tests", testCode);
        out.put("citations", citationList);
        return out;
    }

    private String mode() {
        return Arrays.asList(environment.getActiveProfiles()).contains("pgvector")
                ? "jdbc-postgres" : "jdbc-h2";
    }
}

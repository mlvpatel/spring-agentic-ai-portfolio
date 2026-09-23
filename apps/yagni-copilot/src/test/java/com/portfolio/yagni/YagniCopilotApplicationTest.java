package com.portfolio.yagni;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class YagniCopilotApplicationTest {

    private static final String KEY = "test-yagni-api-key-for-unit-tests-only";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Context loads")
    void contextLoads() {
    }

    @Test
    @DisplayName("Health is UP without skill brands")
    void healthOk() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    org.assertj.core.api.Assertions.assertThat(body)
                            .doesNotContainIgnoringCase("ponytail")
                            .doesNotContainIgnoringCase("gstack")
                            .doesNotContainIgnoringCase("plugin");
                });
    }

    @Test
    @DisplayName("Patch requires API key")
    void patchUnauthorizedWithoutKey() throws Exception {
        mockMvc.perform(post("/api/v1/patch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"changeRequest":"add null check","sourceCode":"class A { void m(){} }"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rejects ey* Bearer bypass")
    void rejectsEyJwtBypass() throws Exception {
        mockMvc.perform(post("/api/v1/patch")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake.sig")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"changeRequest":"x","sourceCode":"class A {}"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rejects valid-test-token bypass")
    void rejectsValidTestTokenBypass() throws Exception {
        mockMvc.perform(post("/api/v1/patch")
                        .header("Authorization", "Bearer valid-test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"changeRequest":"x","sourceCode":"class A {}"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Patch returns offline mode and metrics")
    void patchOk() throws Exception {
        mockMvc.perform(post("/api/v1/patch")
                        .header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "changeRequest": "rename method",
                                  "sourceCode": "public class Demo { public int add(int a, int b) { return a + b; } }",
                                  "maxCyclomatic": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("offline"))
                .andExpect(jsonPath("$.withinBounds").value(true))
                .andExpect(jsonPath("$.cyclomaticComplexity").exists())
                .andExpect(jsonPath("$.patch", containsString("YAGNI patch")))
                .andExpect(jsonPath("$.patch", not(containsString("ponytail"))));
    }

    @Test
    @DisplayName("Blank source fails validation")
    void blankSourceRejected() throws Exception {
        mockMvc.perform(post("/api/v1/patch")
                        .header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"changeRequest":"x","sourceCode":"   "}
                                """))
                .andExpect(status().isBadRequest());
    }
}

package com.portfolio.yagni.service;

import com.portfolio.shared.ai.PortfolioAiClient;
import com.portfolio.yagni.dto.PatchRequest;
import com.portfolio.yagni.dto.PatchResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class YagniPatchServiceTest {

    @Test
    void offlinePathDoesNotClaimLive() {
        PortfolioAiClient ai = mock(PortfolioAiClient.class);
        when(ai.isLive()).thenReturn(false);
        when(ai.assist(eq("patch-explanation"), anyString()))
                .thenReturn("offline-patch-explanation: skipped (no OPENAI_API_KEY / ChatModel)");

        YagniPatchService service = new YagniPatchService(ai);
        PatchResponse response = service.propose(new PatchRequest(
                "add null check",
                "class A { void m(String s) { s.length(); } }",
                5));

        assertThat(response.mode()).isEqualTo("offline");
        assertThat(response.explanation()).startsWith("offline-");
        assertThat(response.notes()).contains(response.explanation());
    }

    @Test
    void livePathSurfacesAssistTextWithoutCallingNetwork() {
        PortfolioAiClient ai = mock(PortfolioAiClient.class);
        when(ai.isLive()).thenReturn(true);
        when(ai.assist(eq("patch-explanation"), anyString()))
                .thenReturn("Keep the null check local; avoid a new helper class.");

        YagniPatchService service = new YagniPatchService(ai);
        PatchResponse response = service.propose(new PatchRequest(
                "add null check",
                "class A { void m(String s) { s.length(); } }",
                5));

        assertThat(response.mode()).isEqualTo("live");
        assertThat(response.explanation()).isEqualTo("Keep the null check local; avoid a new helper class.");
        assertThat(response.notes()).contains(response.explanation());
        assertThat(response.patch()).contains("YAGNI patch");
    }
}

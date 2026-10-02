package com.portfolio.yagni.service;

import com.portfolio.shared.ai.PortfolioAiClient;
import com.portfolio.yagni.dto.PatchRequest;
import com.portfolio.yagni.dto.PatchResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    @Test
    void concurrentProposesDoNotThrow() throws Exception {
        PortfolioAiClient ai = mock(PortfolioAiClient.class);
        when(ai.isLive()).thenReturn(false);
        when(ai.assist(eq("patch-explanation"), anyString()))
                .thenReturn("offline-patch-explanation: skipped (no OPENAI_API_KEY / ChatModel)");

        YagniPatchService service = new YagniPatchService(ai);
        String source = "public class Demo { public int add(int a, int b) { if (a > 0) { return a + b; } return b; } }";
        PatchRequest request = new PatchRequest("rename method", source, 5);
        PatchResponse baseline = service.propose(request);

        int threads = 50;
        int callsEach = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<PatchResponse>> futures = new ArrayList<>();
            for (int i = 0; i < threads * callsEach; i++) {
                futures.add(pool.submit(() -> service.propose(request)));
            }
            List<Throwable> errors = new ArrayList<>();
            for (Future<PatchResponse> future : futures) {
                try {
                    PatchResponse response = future.get(30, TimeUnit.SECONDS);
                    assertThat(response.mode()).isEqualTo("offline");
                    assertThat(response.cyclomaticComplexity()).isEqualTo(baseline.cyclomaticComplexity());
                    assertThat(response.astDepth()).isEqualTo(baseline.astDepth());
                } catch (Throwable error) {
                    errors.add(error);
                }
            }
            if (!errors.isEmpty()) {
                throw new AssertionError(
                        errors.size() + " concurrent patch calls failed; first: " + errors.get(0),
                        errors.get(0));
            }
        } finally {
            pool.shutdownNow();
            assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }
}

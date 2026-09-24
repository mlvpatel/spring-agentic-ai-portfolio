package com.portfolio.yagni.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.portfolio.shared.ai.PortfolioAiClient;
import com.portfolio.yagni.dto.PatchRequest;
import com.portfolio.yagni.dto.PatchResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class YagniPatchService {

    private final JavaParser javaParser;
    private final PortfolioAiClient portfolioAiClient;

    public YagniPatchService(PortfolioAiClient portfolioAiClient) {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);
        this.javaParser = new JavaParser(config);
        this.portfolioAiClient = portfolioAiClient;
    }

    public PatchResponse propose(PatchRequest request) {
        int maxCyclomatic = request.maxCyclomatic() != null ? request.maxCyclomatic() : 5;
        String source = sanitize(request.sourceCode());
        Metrics metrics = measure(source);
        boolean within = metrics.cyclomatic() <= maxCyclomatic;
        List<String> notes = new ArrayList<>();
        if (!within) {
            notes.add("Cyclomatic V(G)=" + metrics.cyclomatic() + " exceeds max " + maxCyclomatic
                    + "; keep the change local and flatten branches.");
        } else {
            notes.add("Within YAGNI cyclomatic bound.");
        }
        if (metrics.astDepth() > 8) {
            notes.add("AST depth " + metrics.astDepth() + " is deep; prefer early returns.");
        }
        notes.add(portfolioAiClient.assist("patch-explanation", request.changeRequest() + "\n" + source));
        String patch = buildPatch(request.changeRequest(), source, within);
        return new PatchResponse(
                resolveMode(),
                request.changeRequest(),
                patch,
                metrics.cyclomatic(),
                metrics.astDepth(),
                maxCyclomatic,
                within,
                notes
        );
    }

    private String resolveMode() {
        // Default offline; live ChatClient only when OPENAI_API_KEY + ChatModel are present.
        return "offline";
    }

    private static String sanitize(String source) {
        String s = source.trim();
        if (s.startsWith("```")) {
            int firstNl = s.indexOf('\n');
            int lastFence = s.lastIndexOf("```");
            if (firstNl > 0 && lastFence > firstNl) {
                return s.substring(firstNl + 1, lastFence).trim();
            }
        }
        return s;
    }

    private Metrics measure(String source) {
        ParseResult<CompilationUnit> parsed = javaParser.parse(source);
        if (!parsed.isSuccessful() || parsed.getResult().isEmpty()) {
            int lines = (int) source.lines().filter(l -> !l.isBlank()).count();
            int heuristic = Math.max(1, lines / 10);
            return new Metrics(heuristic, Math.min(lines, 12));
        }
        CompilationUnit cu = parsed.getResult().get();
        int decisions = cu.findAll(IfStmt.class).size()
                + cu.findAll(ForStmt.class).size()
                + cu.findAll(ForEachStmt.class).size()
                + cu.findAll(WhileStmt.class).size()
                + cu.findAll(DoStmt.class).size()
                + cu.findAll(CatchClause.class).size()
                + cu.findAll(ConditionalExpr.class).size()
                + cu.findAll(SwitchEntry.class).size();
        int cyclomatic = decisions + 1;
        AtomicInteger maxDepth = new AtomicInteger(0);
        walkDepth(cu, 0, maxDepth);
        return new Metrics(cyclomatic, maxDepth.get());
    }

    private static void walkDepth(Node node, int depth, AtomicInteger max) {
        max.updateAndGet(v -> Math.max(v, depth));
        for (Node child : node.getChildNodes()) {
            walkDepth(child, depth + 1, max);
        }
    }

    private static String buildPatch(String changeRequest, String source, boolean within) {
        String header = "// YAGNI patch for: " + changeRequest.strip() + "\n";
        if (!within) {
            return header + "// REFUSED oversized change — split the request or lower complexity first.\n"
                    + "// Original kept unchanged.\n";
        }
        return header
                + "// Apply the smallest edit that satisfies the request.\n"
                + "// Keep using existing types; no new Factory/Builder/Strategy unless required.\n"
                + source
                + (source.endsWith("\n") ? "" : "\n");
    }

    private record Metrics(int cyclomatic, int astDepth) {
    }
}

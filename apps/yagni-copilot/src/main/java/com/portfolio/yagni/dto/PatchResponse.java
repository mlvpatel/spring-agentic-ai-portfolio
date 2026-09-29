package com.portfolio.yagni.dto;

import java.util.List;

public record PatchResponse(
        String mode,
        String changeRequest,
        String patch,
        String explanation,
        int cyclomaticComplexity,
        int astDepth,
        int maxCyclomatic,
        boolean withinBounds,
        List<String> notes
) {
}

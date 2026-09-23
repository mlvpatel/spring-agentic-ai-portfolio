package com.portfolio.yagni.dto;

import jakarta.validation.constraints.NotBlank;

public record PatchRequest(
        @NotBlank String changeRequest,
        @NotBlank String sourceCode,
        Integer maxCyclomatic
) {
}

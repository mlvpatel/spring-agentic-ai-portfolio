package com.portfolio.yagni.web;

import com.portfolio.yagni.dto.PatchRequest;
import com.portfolio.yagni.dto.PatchResponse;
import com.portfolio.yagni.service.YagniPatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PatchController {

    private final YagniPatchService patchService;

    public PatchController(YagniPatchService patchService) {
        this.patchService = patchService;
    }

    @PostMapping("/patch")
    public ResponseEntity<PatchResponse> patch(@Valid @RequestBody PatchRequest request) {
        return ResponseEntity.ok(patchService.propose(request));
    }
}

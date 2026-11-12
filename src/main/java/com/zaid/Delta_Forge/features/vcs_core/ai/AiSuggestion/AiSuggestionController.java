package com.zaid.Delta_Forge.features.vcs_core.ai.AiSuggestion;

import com.zaid.Delta_Forge.features.auth.CustomUserDetails;
import com.zaid.Delta_Forge.features.resolution.Resolution;
import com.zaid.Delta_Forge.features.vcs_core.ai.AiSuggestionResponse;
import com.zaid.Delta_Forge.features.vcs_core.ai.ResolutionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@RestController
@RequestMapping("/merge-requests/{mergeRequestId}/suggestions")
public class AiSuggestionController {

    private final AiSuggestionService suggestionService;

    public AiSuggestionController(AiSuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    public record EditSuggestionRequest(JsonNode newValue) {}

    @PostMapping("/{conflictId}")
    public AiSuggestionResponse suggest(@PathVariable UUID mergeRequestId,
                                        @PathVariable String conflictId) {
        AiSuggestion s = suggestionService.suggest(mergeRequestId, conflictId);
        return toResponse(s);
    }

    @PostMapping("/{suggestionId}/accept")
    public ResolutionResponse accept(@PathVariable UUID mergeRequestId,
                                     @PathVariable UUID suggestionId,
                                     @AuthenticationPrincipal CustomUserDetails user) {
        Resolution r = suggestionService.accept(suggestionId, user.getUsername());
        return toResponse(r);
    }

    @PostMapping("/{suggestionId}/edit")
    public ResolutionResponse edit(@PathVariable UUID mergeRequestId,
                                   @PathVariable UUID suggestionId,
                                   @RequestBody EditSuggestionRequest req,
                                   @AuthenticationPrincipal CustomUserDetails user) {
        Resolution r = suggestionService.edit(suggestionId, req.newValue(), user.getUsername());
        return toResponse(r);
    }

    @PostMapping("/{suggestionId}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable UUID suggestionId,
                       @AuthenticationPrincipal CustomUserDetails user) {
        suggestionService.reject(suggestionId, user.getUsername());
    }

    private AiSuggestionResponse toResponse(AiSuggestion s) {
        return new AiSuggestionResponse(
                s.id(), s.conflictId(), s.action(), s.proposedValue(),
                s.explanation(), s.status(), s.provider(), s.createdAt());
    }

    private ResolutionResponse toResponse(Resolution r) {
        return new ResolutionResponse(
                r.conflictId(), r.resolvedValue(), r.source(), r.resolvedBy(),
                r.resolvedAt(), r.explanation());
    }
}

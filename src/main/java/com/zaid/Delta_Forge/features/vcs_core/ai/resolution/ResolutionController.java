package com.zaid.Delta_Forge.features.vcs_core.ai.resolution;

import com.zaid.Delta_Forge.features.auth.CustomUserDetails;
import com.zaid.Delta_Forge.features.resolution.Resolution;
import com.zaid.Delta_Forge.features.vcs_core.ai.AiSuggestion.AiSuggestion;
import com.zaid.Delta_Forge.features.vcs_core.ai.AiSuggestion.AiSuggestionService;
import com.zaid.Delta_Forge.features.vcs_core.ai.AiSuggestionResponse;
import com.zaid.Delta_Forge.features.vcs_core.ai.ResolutionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/resolutions/{mergeRequestId}")
public class ResolutionController {

    @Autowired
    private ResolutionService resolutionService;

    @GetMapping
    public List<ResolutionRow> getResolutions(@PathVariable UUID mergeRequestId){
        return resolutionService.getResolutions(mergeRequestId);
    }
    @GetMapping("/{conflictId}")
    public ResolutionRow getResolution(@PathVariable UUID mergeRequestId, @PathVariable String conflictId){
        return resolutionService.getResolution(mergeRequestId,conflictId);
    }

}

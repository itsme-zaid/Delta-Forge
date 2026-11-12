package com.zaid.Delta_Forge.features.vcs_core.ai.resolution;

import com.zaid.Delta_Forge.features.resolution.Resolution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ResolutionService {
    @Autowired
    private ResolutionRepository resolutionRepository;

    public List<ResolutionRow> getResolutions(UUID mergeRequestId){
        return resolutionRepository.findByMergeRequestId(mergeRequestId);
    }
    public ResolutionRow getResolution(UUID mergeRequestId,String conflictId){
        return resolutionRepository.findByMergeRequestIdAndConflictId(mergeRequestId,conflictId).orElseThrow(()-> new RuntimeException("Resolution doesn't exist"));
    }


}

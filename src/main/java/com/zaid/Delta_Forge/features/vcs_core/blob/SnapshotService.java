package com.zaid.Delta_Forge.features.vcs_core.blob;

import com.zaid.Delta_Forge.features.json.JsonDocumentService;
import com.zaid.Delta_Forge.features.merge.MergeEngine;
import com.zaid.Delta_Forge.features.vcs_core.repository.RepositoryId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class SnapshotService {
    private final JsonDocumentService jsonService;
    @Autowired private SnapshotRepository repo;
    private final JdbcAggregateTemplate template;
    public SnapshotService(JsonDocumentService jsonService, JdbcAggregateTemplate template){
        this.jsonService = jsonService;
        this.template = template;
    }
    public Snapshot saveSnapshot(JsonNode document){
        String canonical = jsonService.serialize(document);
        byte[] canonicalBytes = canonical.getBytes(StandardCharsets.UTF_8);
        SnapshotHash hash =SnapshotHasher.hash(canonicalBytes);
        return template.insert(new Snapshot(hash,document,"normalization-v1", Instant.now(),canonicalBytes));
    }
    public Snapshot findBySnapshotHash(RepositoryId repositoryId,SnapshotHash snapshotHash){
        return repo.findBySnapshotHash(repositoryId.repository_id(),snapshotHash.hash()).orElseThrow(()-> new RuntimeException("Invalid Snapshot hash"));
    }
}

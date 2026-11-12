package com.zaid.Delta_Forge.features.vcs_core.audit;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepository extends CrudRepository<AuditEvent, UUID> {
    List<AuditEvent> findByMergeRequestIdOrderByCreatedAtAsc(UUID mergeRequestId);

}
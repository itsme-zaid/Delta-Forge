package com.zaid.Delta_Forge.features.vcs_core.merge.merge_request;

import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class MergeRequestService {
    private final MergeRequestRepository repository;
    private final JdbcAggregateTemplate template;

    public MergeRequestService(MergeRequestRepository repository,
                               JdbcAggregateTemplate template) {
        this.repository = repository;
        this.template = template;
    }
    public MergeRequest create(MergeRequest mr) {
        return template.insert(mr);
    }

    public MergeRequest get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("merge request not found: " + id));
    }

    public List<MergeRequest> listByRepo(UUID repoId) {
        return repository.findByRepositoryId(repoId);
    }
}

package com.zaid.Delta_Forge.features.vcs_core.blob;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SnapshotRepository extends CrudRepository<Snapshot,SnapshotHash> {
    Optional<Snapshot> findBySnapshotHash(SnapshotHash snapshotHash);
}

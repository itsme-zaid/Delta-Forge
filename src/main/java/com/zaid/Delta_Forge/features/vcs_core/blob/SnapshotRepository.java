package com.zaid.Delta_Forge.features.vcs_core.blob;

import org.springframework.data.repository.CrudRepository;

public interface SnapshotRepository extends CrudRepository<Snapshot,SnapshotHash> {

}

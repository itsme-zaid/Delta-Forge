package com.zaid.Delta_Forge.features.vcs_core.repository;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

public interface RepositoryRepo extends CrudRepository<Repository,RepositoryId> {
    @Query("SELECT * FROM repositories where user_id= :userId")
    List<Repository> findByUserId(@Param("userId") UUID userId);
}

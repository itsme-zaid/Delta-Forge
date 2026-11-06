package com.zaid.Delta_Forge.features.vcs_core.user;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User,UserId> {
    Optional<User> findByUsername(String username);
}

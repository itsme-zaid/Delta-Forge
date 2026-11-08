package com.zaid.Delta_Forge.features.vcs_core.exceptions;

public class RepositoryNotFoundException extends RuntimeException {
    public RepositoryNotFoundException(String message) {
        super(message);
    }
}

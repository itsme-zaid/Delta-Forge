package com.zaid.Delta_Forge.features.vcs_core.exceptions;

public class StaleHeadException extends RuntimeException {
    public StaleHeadException(String s) {
        super(s);
    }
}

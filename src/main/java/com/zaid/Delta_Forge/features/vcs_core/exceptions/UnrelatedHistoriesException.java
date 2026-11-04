package com.zaid.Delta_Forge.features.vcs_core.exceptions;

import com.zaid.Delta_Forge.features.vcs_core.commit.CommitId;

public class UnrelatedHistoriesException extends RuntimeException {
    public UnrelatedHistoriesException(String invalid) {
        super(invalid);
    }

    public UnrelatedHistoriesException(CommitId ours, CommitId theirs) {
        super(ours.toString() + " " + theirs.toString());
    }
}

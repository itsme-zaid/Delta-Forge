package com.zaid.Delta_Forge.features.vcs_core.dto.user;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    @NonNull
    private String username;
    @NonNull
    private String password;
}

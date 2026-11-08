package com.zaid.Delta_Forge.features.auth;

import com.zaid.Delta_Forge.features.vcs_core.user.User;
import com.zaid.Delta_Forge.features.vcs_core.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserService userService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.get(username).orElseThrow(()->new RuntimeException("User not found"));
        return new CustomUserDetails(
                user.id().user_id(),
                user.username(),
                user.passwordHash()

        );
    }
}

package com.zaid.Delta_Forge.features.vcs_core.user;

import com.zaid.Delta_Forge.features.vcs_core.dto.user.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository repo;
    private JdbcAggregateTemplate template;
    public UserService(JdbcAggregateTemplate template){
        this.template = template;
    }
    private final PasswordEncoder pswdEncoder = new BCryptPasswordEncoder();
    public Optional<User> get(String username){
        return repo.findByUsername(username);
    }
    public User saveNewUser(UserRequest userRequest){
        User user = new User(new UserId(UUID.randomUUID()), userRequest.getUsername(),pswdEncoder.encode(userRequest.getPassword()), Instant.now());
        try{
            template.insert(user);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return user;
    }
}

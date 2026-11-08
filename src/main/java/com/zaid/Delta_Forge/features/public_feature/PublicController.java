package com.zaid.Delta_Forge.features.public_feature;
import com.zaid.Delta_Forge.features.auth.CustomUserDetails;
import com.zaid.Delta_Forge.features.auth.JWTutilService;
import com.zaid.Delta_Forge.features.vcs_core.dto.user.UserRequest;
import com.zaid.Delta_Forge.features.vcs_core.user.User;
import com.zaid.Delta_Forge.features.vcs_core.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/public")
public class PublicController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JWTutilService jwTutilService;
    @Autowired
    private UserService userService;
    private static final Logger log = LoggerFactory.getLogger(PublicController.class);
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserRequest userRequest){
        try{
            User user = userService.saveNewUser(userRequest);
        } catch (RuntimeException e) {
            log.warn(e.getMessage());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequest userRequest){
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userRequest.getUsername(),userRequest.getPassword())
        );

        if(auth.isAuthenticated()){
            log.atInfo()
                    .setMessage("USER AUTHENTICATED")
                    .addKeyValue("username",auth.getName()).log();
            CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();

            return new ResponseEntity<>(jwTutilService.generateToken(customUserDetails), HttpStatus.OK);
        }
        throw new RuntimeException("Invalid Credentials");
    }
}

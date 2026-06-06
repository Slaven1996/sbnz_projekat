package com.ftn.service.controller;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ftn.model.User;
import com.ftn.service.dto.auth.UserTokenState;
import com.ftn.service.dto.user.UserRequest;
import com.ftn.service.dto.user.UserResponse;
import com.ftn.service.security.TokenHelper;
import com.ftn.service.security.auth.JwtAuthenticationRequest;
import com.ftn.service.service.UserService;

@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    private final TokenHelper tokenHelper;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthenticationController(TokenHelper tokenHelper, AuthenticationManager authenticationManager,
                                   UserService userService) {
        this.tokenHelper = tokenHelper;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserTokenState> createAuthenticationToken(
            @RequestBody JwtAuthenticationRequest authenticationRequest) throws AuthenticationException {

        // run the authentication (throws BadCredentialsException on a wrong email/password)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()));

        // store the authenticated user in the context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // create and return the token
        User user = (User) authentication.getPrincipal();
        String jws = tokenHelper.generateToken(user.getEmail());
        int expiresIn = tokenHelper.getExpiredIn();
        String authority = user.getRole().name();

        return ResponseEntity.ok(new UserTokenState(jws, expiresIn, authority));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }
}

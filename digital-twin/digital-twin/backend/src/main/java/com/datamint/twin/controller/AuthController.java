package com.datamint.twin.controller;

import com.datamint.twin.dto.Dtos.*;
import com.datamint.twin.model.AppUser;
import com.datamint.twin.repository.UserRepository;
import com.datamint.twin.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        AppUser u = users.findByUsername(req.username())
                .filter(x -> x.isEnabled() && encoder.matches(req.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        return new LoginResponse(jwt.create(u), info(u));
    }

    @GetMapping("/me")
    public UserInfo me(Authentication auth) {
        return users.findByUsername(auth.getName()).map(AuthController::info)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }

    static UserInfo info(AppUser u) {
        return new UserInfo(u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.isEnabled());
    }
}

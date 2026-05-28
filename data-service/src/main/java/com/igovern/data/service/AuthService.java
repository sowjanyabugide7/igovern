package com.igovern.data.service;

import com.igovern.data.config.JwtUtil;
import com.igovern.data.dto.AuthDtos.LoginRequest;
import com.igovern.data.dto.AuthDtos.RegisterRequest;
import com.igovern.data.dto.AuthDtos.TokenResponse;
import com.igovern.data.entity.User;
import com.igovern.data.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Service
public class AuthService {

    private static final Logger log = LogManager.getLogger(AuthService.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.users = users;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public TokenResponse register(RegisterRequest req) {
        if (users.findByUsername(req.getUsername()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "username already taken");
        }
        User u = new User(req.getUsername(), encoder.encode(req.getPassword()), "ROLE_USER");
        users.save(u);
        log.info("Registered new user '{}'", u.getUsername());
        return new TokenResponse(jwtUtil.generateToken(u.getUsername(), u.getRole()),
                u.getUsername(), u.getRole());
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        User u = users.findByUsername(req.getUsername())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "invalid credentials"));
        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            throw new ResponseStatusException(UNAUTHORIZED, "invalid credentials");
        }
        return new TokenResponse(jwtUtil.generateToken(u.getUsername(), u.getRole()),
                u.getUsername(), u.getRole());
    }
}

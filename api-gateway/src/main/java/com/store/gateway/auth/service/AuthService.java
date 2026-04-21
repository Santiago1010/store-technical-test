package com.store.gateway.auth.service;

import com.store.gateway.auth.dto.AuthResponse;
import com.store.gateway.auth.dto.LoginRequest;
import com.store.gateway.auth.dto.RegisterRequest;
import com.store.gateway.auth.entity.User;
import com.store.gateway.auth.repository.UserRepository;
import com.store.gateway.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil         = jwtUtil;
    }

    public Mono<AuthResponse> register(RegisterRequest req) {
        return userRepository.existsByUsername(req.username())
            .flatMap(userExists -> {
                if (userExists) {
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.CONFLICT, "Username already taken"));
                }
                return userRepository.existsByEmail(req.email());
            })
            .flatMap(emailExists -> {
                if (emailExists) {
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.CONFLICT, "Email already registered"));
                }
                User user = new User(
                    req.username(),
                    req.email(),
                    passwordEncoder.encode(req.password()),
                    DEFAULT_ROLE
                );
                return userRepository.save(user);
            })
            .map(saved -> {
                log.info("event=user_registered username={} role={}", saved.getUsername(), saved.getRole());
                String token = jwtUtil.generateToken(saved.getUsername(), saved.getRole());
                return AuthResponse.of(token, jwtUtil.getExpirationMs(), saved.getUsername(), saved.getRole());
            });
    }

    public Mono<AuthResponse> login(LoginRequest req) {
        return userRepository.findByUsername(req.username())
            .switchIfEmpty(Mono.error(new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, "Invalid credentials")))
            .flatMap(user -> {
                if (!user.isEnabled()) {
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Account disabled"));
                }
                if (!passwordEncoder.matches(req.password(), user.getPassword())) {
                    log.warn("event=login_failed username={}", req.username());
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid credentials"));
                }
                log.info("event=login_success username={}", user.getUsername());
                String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                return Mono.just(AuthResponse.of(
                    token, jwtUtil.getExpirationMs(), user.getUsername(), user.getRole()));
            });
    }
}
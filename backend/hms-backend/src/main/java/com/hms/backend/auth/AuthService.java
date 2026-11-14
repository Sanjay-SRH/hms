package com.hms.backend.auth;

import com.hms.backend.user.AppUser;
import com.hms.backend.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // noRollbackFor: the failed-attempt counter must be saved even though we throw an error
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public LoginResponse login(LoginRequest request) {
        AppUser user = users.findByUsernameIgnoreCase(request.username())
                .orElseThrow(AuthService::invalidCredentials);
        Instant now = Instant.now();

        if (!user.isActive()) {
            throw invalidCredentials();
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.LOCKED,
                    "Account temporarily locked. Try again later.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setLockedUntil(now.plus(LOCK_DURATION));
                user.setFailedLoginAttempts(0);
            }
            users.save(user);
            throw invalidCredentials();
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        users.save(user);

        return new LoginResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.expirationSeconds(),
                user.getUsername(),
                user.roleNames(),
                user.permissionCodes());
    }

    // Same message for unknown user and wrong password, so attackers can't tell which was wrong
    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}
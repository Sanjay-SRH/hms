package com.hms.backend.auth;

import com.hms.backend.user.AppRole;
import com.hms.backend.user.AppRoleRepository;
import com.hms.backend.user.AppUser;
import com.hms.backend.user.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final AppUserRepository users;
    private final AppRoleRepository roles;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.existsByUsernameIgnoreCase("admin")) {
            return;
        }
        AppRole adminRole = roles.findByName("ADMIN").orElseThrow();

        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setEmail("admin@hms.local");
        admin.setFullName("System Administrator");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.getRoles().add(adminRole);
        users.save(admin);

        log.info("Created initial admin user 'admin'");
    }
}
package com.hms.backend.auth;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class MeController {

    //Any logged-in user
    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities().stream().toList());
    }

    // Need the USER_MANAGE permission (ADMIN has it)
    @GetMapping("/admin/ping")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public Map<String, String> adminPing(){
        return Map.of("message", "pong from the admin area");
    }

    //Needs PATIENT_CREATE (REGISTRATION has it, ADMIN does not), so admin should get 403.
    @GetMapping("/registrar/ping")
    @PreAuthorize("hasAuthority('PATIENT_CREATE')")
    public Map<String, String> registrarPing() {
        return Map.of("message", "pong from the registrar area");
    }
}
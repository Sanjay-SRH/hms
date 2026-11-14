package com.hms.backend.auth;

import com.hms.backend.user.AppUser;
import com.hms.backend.user.AppUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Not a @Component on purpose: SecurityConfig creates it, so it runs only once per request.
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppUserRepository users;

    public JwtAuthenticationFilter(JwtService jwtService, AppUserRepository users) {
        this.jwtService = jwtService;
        this.users = users;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                UUID userId = UUID.fromString(claims.getSubject());

                // Reload from the database so deactivation and role changes apply immediately
                AppUser user = users.findById(userId).orElse(null);
                if (user != null && user.isActive()) {
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    for (String role : user.roleNames()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                    }
                    for (String permission : user.permissionCodes()) {
                        authorities.add(new SimpleGrantedAuthority(permission));
                    }
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    user.getUsername(), null, authorities));
                }
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext(); // bad or expired token: stay unauthenticated
            }
        }
        chain.doFilter(request, response);
    }
}
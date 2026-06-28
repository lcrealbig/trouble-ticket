package com.example.ticketer.controller;

import com.example.ticketer.security.JwtService;
import com.example.ticketer.service.TenantService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TenantService tenantService;

    @PostMapping("/token")
    public ResponseEntity<String> generateToken(@RequestHeader("Authorization")
                                                String authorizationHeader,
                                                HttpServletRequest request) {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Basic ")) {
            log.warn("Invalid authorization header on request: {}",
                    request.getRequestURI() + "?" + request.getQueryString() + " from " + request.getRemoteAddr());
            return ResponseEntity.badRequest().body("Invalid authorization header");
        }

        var base64Credentials = authorizationHeader.substring("Basic ".length()).trim();
        var decodedBytes = Base64.getDecoder().decode(base64Credentials);
        var credentials = new String(decodedBytes);
        var values = credentials.split(":", 2);

        if (values.length != 2) {
            return ResponseEntity.badRequest().body("Invalid credentials format");
        }

        var username = values[0];
        var password = values[1];

        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            var userDetails = (UserDetails) authentication.getPrincipal();
            // Try to find tenant by ID first (email as tenant ID), then by name as fallback
            var tenant = tenantService.findTenantById(username)
                    .orElseGet(() -> tenantService.getTenantByName(username));
            var token = jwtService.generateToken(userDetails, tenant.getId());

            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }
}
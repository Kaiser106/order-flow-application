package com.orderflow.auth.controller;

import com.orderflow.auth.dto.AuthResponse;
import com.orderflow.auth.dto.LoginRequest;
import com.orderflow.auth.dto.RegisterRequest;
import com.orderflow.auth.service.AuthService;
import com.orderflow.auth.service.CustomUserDetails;
import com.orderflow.common.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<Result<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        Result<AuthResponse> result = authService.register(request);
        return result.isSuccess() ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @PostMapping("/login")
    public ResponseEntity<Result<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );


        SecurityContextHolder.getContext().setAuthentication(authentication);


        HttpSession session = httpRequest.getSession(true);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        AuthResponse response = new AuthResponse(
                userDetails.getUser().getId(),
                userDetails.getUser().getEmail(),
                userDetails.getUser().getRole().name()
        );

        return ResponseEntity.ok(Result.success(response, "Login successful."));
    }

    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Result.success(null, "Logout successful."));
    }

    @GetMapping("/session")
    public ResponseEntity<Result<AuthResponse>> getSession() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            AuthResponse response = new AuthResponse(
                    userDetails.getUser().getId(),
                    userDetails.getUser().getEmail(),
                    userDetails.getUser().getRole().name()
            );
            return ResponseEntity.ok(Result.success(response));
        }
        return ResponseEntity.status(401).body(Result.failure("No active session."));
    }
}
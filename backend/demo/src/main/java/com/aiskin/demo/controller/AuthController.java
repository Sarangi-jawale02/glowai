package com.aiskin.demo.controller;

import com.aiskin.demo.dto.SigninRequest;
import com.aiskin.demo.dto.SignupRequest;
import com.aiskin.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allows requests from the local static HTML file
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest request) {
        String response = authService.signup(request);
        if ("Signup successful".equals(response)) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<String> signin(@RequestBody SigninRequest request) {
        String response = authService.signin(request);
        if ("Login successful".equals(response)) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}

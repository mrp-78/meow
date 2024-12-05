package com.social.meow.controller;

import com.social.meow.service.JwtService;
import com.social.meow.service.OtpService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final OtpService otpService;
    private final JwtService jwtService;

    public AuthController(OtpService otpService, JwtService jwtService) {
        this.otpService = otpService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.badRequest().body("Phone number is required");
        }

        otpService.generateAndSendOtp(phoneNumber);
        return ResponseEntity.ok("OTP sent to your phone");
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestBody Map<String, String> request, HttpServletResponse response) {
        String phoneNumber = request.get("phoneNumber");
        String otp = request.get("otp");

        if (phoneNumber == null || otp == null) {
            return ResponseEntity.badRequest().body("Phone number and OTP are required");
        }

        boolean isValid = otpService.validateOtp(phoneNumber, otp);
        if (isValid) {
            // Generate JWT token
            String jwtToken = jwtService.generateToken(phoneNumber);

            // Set JWT token in HttpOnly, Secure cookie (cookie is only sent over HTTPS)
            Cookie cookie = new Cookie("JWT", jwtToken);
            cookie.setHttpOnly(true);
            cookie.setSecure(true); // Set to true in production with HTTPS enabled
            cookie.setPath("/");
            cookie.setMaxAge(86400); // 1 day expiration time for the cookie

            response.addCookie(cookie);

            return ResponseEntity.ok("Authentication successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
        }
    }
}

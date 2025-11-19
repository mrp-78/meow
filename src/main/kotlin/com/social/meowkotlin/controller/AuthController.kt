package com.social.meowkotlin.controller

import com.social.meowkotlin.service.AuthService
import com.social.meowkotlin.service.OtpService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(otpService: OtpService, authService: AuthService) {
    private val otpService: OtpService = otpService
    private val authService: AuthService = authService

    @PostMapping("/login")
    fun login(@RequestBody request: Map<String?, String?>): ResponseEntity<String> {
        val phoneNumber = request["phoneNumber"]
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.badRequest().body<String>("Phone number is required")
        }

        otpService.generateAndSendOtp(phoneNumber)
        return ResponseEntity.ok<String>("OTP sent to your phone")
    }

    @PostMapping("/confirm")
    fun confirm(@RequestBody request: Map<String?, String?>, response: HttpServletResponse): ResponseEntity<String> {
        val phoneNumber = request["phoneNumber"]
        val otp = request["otp"]

        if (phoneNumber == null || otp == null) {
            return ResponseEntity.badRequest().body<String>("Phone number and OTP are required")
        }

        val isValid: Boolean = otpService.validateOtp(phoneNumber, otp)
        if (isValid) {
            // Generate JWT token
            val jwtToken: String = authService.generateToken(phoneNumber)

            val cookie = Cookie("JWT", jwtToken)
            cookie.isHttpOnly = true
            cookie.secure = true
            cookie.path = "/"
            cookie.maxAge = 86400 // 1 day expiration time for the cookie

            response.addCookie(cookie)

            return ResponseEntity.ok<String>("Authentication successful")
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body<String>("Invalid OTP")
        }
    }
}

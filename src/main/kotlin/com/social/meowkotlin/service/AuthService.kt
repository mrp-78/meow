package com.social.meowkotlin.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService {
    @Value("\${jwt.secretKey}")
    private val secretKey: String? = null

    @Value("\${jwt.expirationTime}")
    private val expirationTime: Long = 0

    fun generateToken(phoneNumber: String?): String {
        val key = Keys.hmacShaKeyFor(secretKey?.toByteArray())
        return Jwts.builder()
            .setSubject(phoneNumber)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationTime))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun extractPhoneNumber(token: String?): String {
        val key = Keys.hmacShaKeyFor(secretKey?.toByteArray())
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun isTokenExpired(token: String?): Boolean {
        val key = Keys.hmacShaKeyFor(secretKey?.toByteArray())
        val expiration = Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
            .expiration
        return expiration.before(Date())
    }
}

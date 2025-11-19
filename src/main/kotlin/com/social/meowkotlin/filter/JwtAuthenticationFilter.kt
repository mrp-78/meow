package com.social.meowkotlin.filter

import com.social.meowkotlin.service.AuthService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(authService: AuthService) : OncePerRequestFilter() {
    private val authService: AuthService = authService

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = getJwtFromCookies(request)

        if (token != null && !authService.isTokenExpired(token)) {
            val phoneNumber: String = authService.extractPhoneNumber(token)
            SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken(
                    phoneNumber,
                    null,
                    ArrayList<GrantedAuthority>()
                )
            )
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromCookies(request: HttpServletRequest): String? {
        val cookies = request.cookies
        if (cookies != null) {
            for (cookie in cookies) {
                if ("JWT" == cookie.name) {
                    return cookie.value
                }
            }
        }
        return null
    }
}

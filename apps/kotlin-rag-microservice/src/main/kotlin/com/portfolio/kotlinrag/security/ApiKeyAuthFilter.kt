package com.portfolio.kotlinrag.security

import com.portfolio.kotlinrag.config.ApiKeyProperties
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiKeyAuthFilter(private val properties: ApiKeyProperties) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.requestURI.startsWith("/actuator/")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val presented = extractKey(request)
        if (presented == null || presented != properties.apiKey) {
            response.status = HttpStatus.UNAUTHORIZED.value()
            response.contentType = "application/json"
            response.writer.write("""{"error":"unauthorized"}""")
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun extractKey(request: HttpServletRequest): String? {
        request.getHeader("X-API-Key")?.takeIf { it.isNotBlank() }?.let { return it.trim() }
        val auth = request.getHeader("Authorization")
        if (auth != null && auth.regionMatches(0, "Bearer ", 0, 7, ignoreCase = true)) {
            return auth.substring(7).trim()
        }
        return null
    }
}

package com.tennisfolio.Tennisfolio.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.tennisfolio.Tennisfolio.util.CookieUtils.getCookie;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = resolveAccessToken(request);

        if(token == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try{
            jwtTokenProvider.validateOrThrow(token);

            Authentication authentication =
                    jwtTokenProvider.getAuthentication(token);

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
            filterChain.doFilter(request, response);

        }catch(ExpiredJwtException e){
            sendUnauthorizedResponse(response, "ACCESS_TOKEN_EXPIRED");
        }catch(JwtException | IllegalArgumentException e){
            sendUnauthorizedResponse(response, "INVALID_ACCESS_TOKEN");
        }

    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String responseBody = String.format("{\"error\": \"%s\"}", message);
        response.getWriter().write(responseBody);
    }

    private String resolveAccessToken(HttpServletRequest request) {

        // 1️⃣ Authorization Header
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        // 2️⃣ Cookie fallback
        return getCookie(request, "access_token");
    }


}

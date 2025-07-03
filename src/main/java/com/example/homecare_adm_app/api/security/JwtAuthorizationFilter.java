package com.example.homecare_adm_app.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Regex para endpoints públicos POST 
    private static final Pattern PUBLIC_POST_ENDPOINTS = Pattern.compile(
            "^/auth/(login|register|forgot-password|reset-password)/?$"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        final String method = request.getMethod();

        log.info("Request URI: {}, Method: {}", requestURI, method);

       
        if ("OPTIONS".equals(method)) {
            log.info("OPTIONS request detected, allowing passage");
            filterChain.doFilter(request, response);
            return;
        }

        // Verificar endpoints públicos
        if (isPublicEndpoint(requestURI, method)) {
            log.info("Public endpoint detected: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        log.debug("Authorization header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No valid Authorization header found for URI: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: No valid token provided");
            return;
        }

        final String jwt = authHeader.substring(7);
        final String userEmail;

        try {
            userEmail = jwtService.extractUsername(jwt);
            log.info("JWT token extracted for user: {}", userEmail);
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Invalid token");
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                log.info("User details loaded: {}", userDetails.getUsername());

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("Authentication successful for user: {}", userDetails.getUsername());
                    filterChain.doFilter(request, response);
                } else {
                    log.warn("Invalid token for user: {}", userEmail);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Unauthorized: Token validation failed");
                }
            } catch (Exception e) {
                log.error("Error during authentication: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized: Authentication error");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isPublicEndpoint(String uri, String method) {
       
        if ("POST".equals(method) && PUBLIC_POST_ENDPOINTS.matcher(uri).matches()) {
            return true;
        }
        
        if ("GET".equals(method) && (uri.startsWith("/api-docs") || uri.startsWith("/swagger-ui"))) {
            return true;
        }
       
        if ("OPTIONS".equals(method)) {
            return true;
        }
        return false;
    }
}
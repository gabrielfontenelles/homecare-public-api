package com.example.homecare_adm_app.api.security;

import com.example.homecare_adm_app.api.dto.AuthRequestDTO;
import com.example.homecare_adm_app.api.dto.AuthResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            AuthRequestDTO loginRequest = new ObjectMapper().readValue(request.getInputStream(), AuthRequestDTO.class);
            
            System.out.println("Tentativa de login: " + loginRequest.getEmail());
            
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getSenha()
                    )
            );
        } catch (IOException e) {
            System.err.println("Erro ao processar requisição de login: " + e.getMessage());
            throw new BadCredentialsException("Erro ao processar credenciais: " + e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        try {
            String username = authResult.getName();
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            String token = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);
            
            AuthResponseDTO authResponse = AuthResponseDTO.builder()
                    .token(token)
                    .refreshToken(refreshToken)
                    .build();
            
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            
            System.out.println("Login bem-sucedido para: " + username);
        } catch (Exception e) {
            System.err.println("Erro no processamento de login bem-sucedido: " + e.getMessage());
            e.printStackTrace();
            
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            new ObjectMapper().writeValue(response.getOutputStream(), 
                    Map.of("error", "Erro interno ao processar login: " + e.getMessage()));
        }
    }
    
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        System.err.println("Login malsucedido: " + failed.getMessage());
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), 
                Map.of("error", "Credenciais inválidas: " + failed.getMessage()));
    }
}
package com.claro.desafiopedidos.service;

import com.claro.desafiopedidos.config.JwtProperties;
import com.claro.desafiopedidos.dto.request.LoginRequest;
import com.claro.desafiopedidos.dto.response.LoginResponse;
import com.claro.desafiopedidos.exception.UnauthorizedExecption;
import com.claro.desafiopedidos.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            log.info("Login realizado com sucesso: email={}", request.email());
            return new LoginResponse(token, "Bearer", 3600000);
        } catch (BadCredentialsException e) {
            log.warn("Falha no login: credenciais inválidas: ", request.email());

            throw new UnauthorizedExecption("Email ou senha inválidos");

        } catch (AuthenticationException e) {
            log.warn("Falha de autenticação: email={}, reason={}", request.email(), e.getClass().getSimpleName());

            throw new UnauthorizedExecption("Email ou senha inválidos");
        }
    }
}

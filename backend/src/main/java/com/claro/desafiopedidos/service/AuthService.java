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
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            log.info(
                    "event=login_success email={} tokenType={} expiresInMs={}",
                    request.email(),
                    "Bearer",
                    jwtProperties.expiration()
            );
            return new LoginResponse(token, "Bearer", jwtProperties.expiration());
        } catch (BadCredentialsException exception) {
            log.warn("event=login_failed email={} reason=bad_credentials", request.email());
            throw new UnauthorizedExecption("Email ou senha inválidos");
        } catch (AuthenticationException exception) {
            log.warn(
                    "event=login_failed email={} reason={}",
                    request.email(),
                    exception.getClass().getSimpleName()
            );
            throw new UnauthorizedExecption("Email ou senha inválidos");
        }
    }
}

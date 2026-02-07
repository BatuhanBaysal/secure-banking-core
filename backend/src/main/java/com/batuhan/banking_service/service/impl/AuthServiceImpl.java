package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.dto.request.AuthRequest;
import com.batuhan.banking_service.dto.response.AuthResponse;
import com.batuhan.banking_service.entity.UserEntity;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.repository.UserRepository;
import com.batuhan.banking_service.security.JwtService;
import com.batuhan.banking_service.security.UserDetailsImpl;
import com.batuhan.banking_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse authenticate(AuthRequest request) {
        performAuthentication(request.getEmail(), request.getPassword());

        UserEntity userEntity = findUserByEmail(request.getEmail());
        String jwtToken = createToken(userEntity);

        return buildAuthResponse(jwtToken);
    }

    private void performAuthentication(String email, String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (BadCredentialsException e) {
            throw new BankingServiceException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }

    private UserEntity findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BankingServiceException("User not found", HttpStatus.NOT_FOUND));
    }

    private String createToken(UserEntity userEntity) {
        var userPrincipal = new UserDetailsImpl(userEntity);
        return jwtService.generateToken(userPrincipal);
    }

    private AuthResponse buildAuthResponse(String token) {
        return AuthResponse.builder()
                .token(token)
                .build();
    }
}
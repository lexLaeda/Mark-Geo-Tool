package com.geo.easypoint.iml.security;

import com.geo.easypoint.api.users.request.CreateUserRequest;
import com.geo.easypoint.api.users.request.LoginRequest;
import com.geo.easypoint.api.users.response.AuthenticationResponse;
import com.geo.easypoint.api.users.response.LoginResponse;
import com.geo.easypoint.iml.employee.repository.EmployeeRepository;
import com.geo.easypoint.iml.mapper.EasyPointMapper;
import com.geo.easypoint.iml.users.entity.EasyPointUser;
import com.geo.easypoint.iml.users.entity.Token;
import com.geo.easypoint.iml.users.entity.TokenType;
import com.geo.easypoint.iml.users.repository.EasyPointUserRepository;
import com.geo.easypoint.iml.users.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final EmployeeRepository employeeRepository;
    private final EasyPointUserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public LoginResponse register(CreateUserRequest request) {
        EasyPointUser user = EasyPointMapper.toEasyPointUser(request.username(), employeeRepository.findById(request.employeeId()).orElse(null), passwordEncoder.encode(request.password()));
        var savedUser = repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        saveUserToken(savedUser, jwtToken);
        return new LoginResponse(request.username(), jwtToken);
    }

    public AuthenticationResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        EasyPointUser user = repository.findByUsername(request.username())
                .orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return EasyPointMapper.toAuthenticationResponse(user, jwtToken);
    }

    private void saveUserToken(EasyPointUser user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(EasyPointUser user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
}
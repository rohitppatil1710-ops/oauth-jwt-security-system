package com.security.service;

import com.security.dto.LoginRequest;
import com.security.dto.LoginResponse;
import com.security.entity.RefreshToken;
import com.security.entity.Role;
import com.security.entity.User;
import com.security.exception.CustomException;
import com.security.repository.RefreshTokenRepository;
import com.security.repository.UserRepository;
import com.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtUtil jwtUtil,
                       AuditLogService auditLogService,
                       TokenBlacklistService tokenBlacklistService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.auditLogService = auditLogService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public LoginResponse login(LoginRequest request, String ipAddress) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        List<String> roles = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getPermissionName())
                .distinct()
                .collect(Collectors.toList());

        String tenantName = user.getTenant() != null ? user.getTenant().getTenantName() : "default";
        String accessToken = jwtUtil.generateToken(user.getUsername(), tenantName, roles, permissions);
        String refreshToken = createRefreshToken(user);

        auditLogService.log(user.getUsername(), "LOGIN", ipAddress, true);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUsername(user.getUsername());
        response.setTenantName(tenantName);
        response.setRoles(roles);
        response.setPermissions(permissions);
        return response;
    }

    public LoginResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException("Invalid refresh token", HttpStatus.UNAUTHORIZED));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new CustomException("Refresh token expired", HttpStatus.UNAUTHORIZED);
        }

        User user = token.getUser();
        List<String> roles = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(p -> p.getPermissionName())
                .distinct()
                .collect(Collectors.toList());

        String tenantName = user.getTenant() != null ? user.getTenant().getTenantName() : "default";
        String newAccessToken = jwtUtil.generateToken(user.getUsername(), tenantName, roles, permissions);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(refreshToken);
        response.setUsername(user.getUsername());
        response.setTenantName(tenantName);
        response.setRoles(roles);
        response.setPermissions(permissions);
        return response;
    }

    public void logout(String token, String username, String ipAddress) {
        tokenBlacklistService.blacklistToken(token, jwtUtil.getExpirationFromToken(token));
        userRepository.findByUsername(username).ifPresent(user ->
                refreshTokenRepository.deleteByUser(user)
        );
        auditLogService.log(username, "LOGOUT", ipAddress, true);
    }

    private String createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
        return refreshTokenRepository.save(refreshToken).getToken();
    }
}
package com.security.service;

import com.security.dto.UserRequest;
import com.security.dto.UserResponse;
import com.security.entity.Role;
import com.security.entity.Tenant;
import com.security.entity.User;
import com.security.exception.CustomException;
import com.security.repository.RoleRepository;
import com.security.repository.TenantRepository;
import com.security.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       TenantRepository tenantRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(UserRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(
                    "Username already exists",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(
                    "Email already exists",
                    HttpStatus.BAD_REQUEST
            );
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        if (request.getTenantName() != null) {
            Tenant tenant = tenantRepository
                    .findByTenantName(request.getTenantName())
                    .orElseThrow(() ->
                            new CustomException(
                                    "Tenant not found",
                                    HttpStatus.NOT_FOUND
                            ));

            user.setTenant(tenant);
        }

        Set<Role> roles = new HashSet<>();

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {

            request.getRoles().forEach(roleName -> {

                Role role = roleRepository.findByRoleName(roleName)
                        .orElseThrow(() ->
                                new CustomException(
                                        "Role not found: " + roleName,
                                        HttpStatus.NOT_FOUND
                                ));

                roles.add(role);
            });

        } else {

            roleRepository.findByRoleName("USER")
                    .ifPresent(roles::add);
        }

        user.setRoles(roles);

        return mapToResponse(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() ->
                        new CustomException(
                                "Current user not found",
                                HttpStatus.NOT_FOUND
                        ));

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        List<User> users;

        if (isAdmin) {

            users = userRepository.findAll();

        } else {

            if (currentUser.getTenant() == null) {
                throw new CustomException(
                        "Tenant information missing",
                        HttpStatus.FORBIDDEN
                );
            }

            users = userRepository.findByTenantId(
                    currentUser.getTenant().getId()
            );
        }

        return users.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {

        User targetUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new CustomException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        ));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String currentUsername = authentication.getName();

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() ->
                        new CustomException(
                                "Current user not found",
                                HttpStatus.NOT_FOUND
                        ));

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {

            if (currentUser.getTenant() == null ||
                    targetUser.getTenant() == null) {

                throw new CustomException(
                        "Tenant information missing",
                        HttpStatus.FORBIDDEN
                );
            }

            String currentTenant =
                    currentUser.getTenant().getTenantName();

            String targetTenant =
                    targetUser.getTenant().getTenantName();

            if (!currentTenant.equals(targetTenant)) {

                throw new CustomException(
                        "Cross-tenant access denied",
                        HttpStatus.FORBIDDEN
                );
            }
        }

        return mapToResponse(targetUser);
    }

    public UserResponse updateUser(Long id, UserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new CustomException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        ));

        user.setEmail(request.getEmail());

        if (request.getPassword() != null &&
                !request.getPassword().isEmpty()) {

            user.setPassword(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        return mapToResponse(userRepository.save(user));
    }

    public void deleteUser(Long id) {

        if (!userRepository.existsById(id)) {
            throw new CustomException(
                    "User not found",
                    HttpStatus.NOT_FOUND
            );
        }

        userRepository.deleteById(id);
    }

    private UserResponse mapToResponse(User user) {

        UserResponse response = new UserResponse();

        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setEnabled(user.isEnabled());

        if (user.getTenant() != null) {
            response.setTenantName(
                    user.getTenant().getTenantName()
            );
        }

        response.setRoles(
                user.getRoles()
                        .stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toList())
        );

        return response;
    }
}
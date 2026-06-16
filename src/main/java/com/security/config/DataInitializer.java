package com.security.config;

import com.security.entity.*;
import com.security.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Create Permissions
            Permission readPerm = getOrCreatePermission(permissionRepository, "READ");
            Permission writePerm = getOrCreatePermission(permissionRepository, "WRITE");
            Permission deletePerm = getOrCreatePermission(permissionRepository, "DELETE");
            Permission adminPerm = getOrCreatePermission(permissionRepository, "ADMIN");

            // Create Roles
            Role adminRole = getOrCreateRole(roleRepository, "ADMIN");
            Set<Permission> adminPerms = new HashSet<>();
            adminPerms.add(readPerm);
            adminPerms.add(writePerm);
            adminPerms.add(deletePerm);
            adminPerms.add(adminPerm);
            adminRole.setPermissions(adminPerms);
            roleRepository.save(adminRole);

            Role userRole = getOrCreateRole(roleRepository, "USER");
            Set<Permission> userPerms = new HashSet<>();
            userPerms.add(readPerm);
            userPerms.add(writePerm);
            userRole.setPermissions(userPerms);
            roleRepository.save(userRole);

            Role managerRole = getOrCreateRole(roleRepository, "MANAGER");
            Set<Permission> managerPerms = new HashSet<>();
            managerPerms.add(readPerm);
            managerPerms.add(writePerm);
            managerPerms.add(deletePerm);
            managerRole.setPermissions(managerPerms);
            roleRepository.save(managerRole);

            // Create Tenants
            Tenant tenant1 = getOrCreateTenant(tenantRepository, "TenantA");
            Tenant tenant2 = getOrCreateTenant(tenantRepository, "TenantB");

            // Create Admin User
            if (!userRepository.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@tenantA.com");
                admin.setTenant(tenant1);
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                admin.setRoles(roles);
                userRepository.save(admin);
                System.out.println("Admin user created: admin / admin123");
            }

            // Create Regular User
            if (!userRepository.existsByUsername("user1")) {
                User user = new User();
                user.setUsername("user1");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setEmail("user1@tenantA.com");
                user.setTenant(tenant1);
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                user.setRoles(roles);
                userRepository.save(user);
                System.out.println("Regular user created: user1 / user123");
            }

            // Create Manager User
            if (!userRepository.existsByUsername("manager1")) {
                User manager = new User();
                manager.setUsername("manager1");
                manager.setPassword(passwordEncoder.encode("manager123"));
                manager.setEmail("manager1@tenantB.com");
                manager.setTenant(tenant2);
                Set<Role> roles = new HashSet<>();
                roles.add(managerRole);
                manager.setRoles(roles);
                userRepository.save(manager);
                System.out.println("Manager user created: manager1 / manager123");
            }
        };
    }

    private Permission getOrCreatePermission(PermissionRepository repo, String name) {
        return repo.findByPermissionName(name).orElseGet(() -> {
            Permission p = new Permission();
            p.setPermissionName(name);
            return repo.save(p);
        });
    }

    private Role getOrCreateRole(RoleRepository repo, String name) {
        return repo.findByRoleName(name).orElseGet(() -> {
            Role r = new Role();
            r.setRoleName(name);
            return repo.save(r);
        });
    }

    private Tenant getOrCreateTenant(TenantRepository repo, String name) {
        return repo.findByTenantName(name).orElseGet(() -> {
            Tenant t = new Tenant();
            t.setTenantName(name);
            return repo.save(t);
        });
    }
}
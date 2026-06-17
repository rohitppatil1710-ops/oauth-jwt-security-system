package com.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.UUID;

@Configuration
public class RegisteredClientConfig {

    /**
     * Configure OAuth2 Registered Clients
     * In production, store these in database instead of memory
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        
        // Client 1: Web Application with Authorization Code Flow
        RegisteredClient webClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("web-client")
                .clientSecret("{noop}web-secret-12345")  // Use BCrypt in production
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:3000/callback")
                .redirectUri("http://localhost:8080/login/oauth2/code/web-client")
                .scope("openid")
                .scope("profile")
                .scope("email")
                .scope("write")
                .scope("read")
                .clientName("Web Client")
                .build();

        // Client 2: Mobile Application with PKCE (Public Client)
        RegisteredClient mobileClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("mobile-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("com.example.mobile://oauth2/callback")
                .scope("openid")
                .scope("profile")
                .scope("email")
                .scope("write")
                .scope("read")
                .clientName("Mobile Client")
                .build();

        // Client 3: Microservice with Client Credentials Flow
        RegisteredClient serviceClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("service-client")
                .clientSecret("{noop}service-secret-67890")  // Use BCrypt in production
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("write")
                .scope("read")
                .clientName("Microservice Client")
                .build();

        // Client 4: Admin Panel
        RegisteredClient adminClient = RegisteredClient
                .withId(UUID.randomUUID().toString())
                .clientId("admin-client")
                .clientSecret("{noop}admin-secret-11111")  // Use BCrypt in production
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri("http://localhost:4200/admin/callback")
                .scope("openid")
                .scope("profile")
                .scope("email")
                .scope("write")
                .scope("read")
                .scope("admin")
                .clientName("Admin Panel")
                .build();

        return new InMemoryRegisteredClientRepository(webClient, mobileClient, serviceClient, adminClient);
    }
}
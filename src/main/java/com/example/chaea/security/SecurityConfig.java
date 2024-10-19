package com.example.chaea.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest.Builder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;



@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/", "/docs/**", "/api-docs/**", "/swagger-ui/**", "/health/**", "/login/**", "/oauth2/**").permitAll()
            .requestMatchers("/test/**").hasAuthority("ESTUDIANTE")
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth2 -> oauth2
            .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(customAuthorizationRequestResolver(clientRegistrationRepository)))
            .successHandler(customAuthenticationSuccessHandler())            
        );
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    
 // Custom Authorization Request Resolver para modificar el estado
    private OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver defaultResolver = 
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
                return customizeAuthorizationRequest(request, authorizationRequest);
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
                return customizeAuthorizationRequest(request, authorizationRequest);
            }

            // Customización para agregar el userType al state
            private OAuth2AuthorizationRequest customizeAuthorizationRequest(HttpServletRequest request, OAuth2AuthorizationRequest authorizationRequest) {
                if (authorizationRequest != null) {
                    // Obtener el userType desde la URL o los parámetros de la consulta
                    String userType = request.getParameter("userType");
                    if (userType == null) {
                        // Asignar userType por defecto si no se recibe en la solicitud
                        userType = "default";
                    }

                    // Combinar userType con el estado (state)
                    String state = "userType=" + userType;

                    // Crear la nueva solicitud con el estado modificado
                    Builder authorizationRequestBuilder = OAuth2AuthorizationRequest.from(authorizationRequest);
                    authorizationRequestBuilder.state(state);  // Añadir el userType al state
                    return authorizationRequestBuilder.build();
                }
                return authorizationRequest;
            }
        };
    }

    // Custom Success Handler para recibir el estado en la respuesta
    private AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // Obtener el valor del state de la redirección de Google
            String state = request.getParameter("state");

            // Parsear el state para obtener el userType
            String userType = null;
            if (state != null && state.contains("userType=")) {
                userType = state.split("userType=")[1];  // Extraer el valor de userType
            }

            // Lógica personalizada para manejar diferentes tipos de usuarios
            if ("profesor".equals(userType)) {
                System.out.println("Usuario es un profesor");
                // Redirigir al dashboard de profesor
                response.sendRedirect("/auth/login/success/prof");
            } else if ("estudiante".equals(userType)) {
                System.out.println("Usuario es un estudiante");
                // Redirigir al dashboard de estudiante
                response.sendRedirect("/auth/login/success/estud");
            } else {
                // Si no se encuentra userType, redirigir a un dashboard genérico
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    JwtDecoder jwtDecoder() {
        // Usa NimbusJwtDecoder para decodificar el token JWT, debes configurar la URI del emisor
        return NimbusJwtDecoder.withJwkSetUri("https://www.googleapis.com/oauth2/v3/certs").build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        // Configura aquí la conversión de claims, roles o cualquier otra lógica
        return converter;
    }
    
   
    
}
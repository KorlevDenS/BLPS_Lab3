package org.korolev.dens.blps_lab1.configuration;

import org.korolev.dens.blps_lab1.filters.ExceptionHandlerFilter;
import org.korolev.dens.blps_lab1.filters.JwtAuthFilter;
import org.korolev.dens.blps_lab1.repositories.ClientRepository;
import org.korolev.dens.blps_lab1.repositories.PermissionRepository;
import org.korolev.dens.blps_lab1.repositories.RoleRepository;
import org.korolev.dens.blps_lab1.security.ClientService;
import org.korolev.dens.blps_lab1.services.JwtTokenService;
import org.korolev.dens.blps_lab1.services.PasswordService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ExceptionHandlerFilter handlerFilter;

    private final PasswordService passwordService;
    private final ClientRepository clientRepository;
    private final JwtTokenService jwtTokenService;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, ExceptionHandlerFilter handlerFilter,
                          ClientRepository clientRepository, PermissionRepository permissionRepository,
                          RoleRepository roleRepository,
                          PasswordService passwordService, JwtTokenService jwtTokenService) {
        this.permissionRepository = permissionRepository;
        this.jwtTokenService = jwtTokenService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.passwordService = passwordService;
        this.clientRepository = clientRepository;
        this.roleRepository = roleRepository;
        this.handlerFilter = handlerFilter;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new ClientService(this.passwordService, this.clientRepository,
                this.jwtTokenService, this.permissionRepository, this.roleRepository);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                        "client/register",
                        "client/login",
                        "chapter/get/all",
                        "comment/get/all/by/topic/*",
                        "topic/get/all/by/chapter/*",
                        "topic/get/by/id/*",
                        "comment/test/message",
                        "topic/get/image/*").permitAll())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(handlerFilter, JwtAuthFilter.class)
                .build();
    }

    @Bean
    static RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(
                """
                        ROLE_ADMIN > ROLE_MODER
                        ROLE_MODER > ROLE_USER
                        """);
        return hierarchy;
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}

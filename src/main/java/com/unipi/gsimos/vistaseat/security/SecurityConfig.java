package com.unipi.gsimos.vistaseat.security;


import com.unipi.gsimos.vistaseat.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository _userRepository;

    public SecurityConfig(UserRepository userRepository) {
        _userRepository = userRepository;

    }

    @Bean
    public UserDetailsService userDetailsService() { return new CustomUserDetailsService(_userRepository); }

    // Used for storing encrypted passwords in the database and login
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthFailureHandler customAuthFailureHandler)
            throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(httpForm -> {
                            httpForm.loginPage("/adminLogin")
                                    .usernameParameter("email")
                                    .passwordParameter("password")
                                    .defaultSuccessUrl("/adminDashboard", true)
                                    .failureHandler(customAuthFailureHandler)
                                    .permitAll();// endpoints that are publicly accessible

                        })
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests//creating exceptions for URLs we want to allow access

                            // Public API registration for new users
                            .requestMatchers("/api/users/register").permitAll()

                            // Public pages
                            .requestMatchers("/","/adminLogin","/adminLogin/**","/userSignUp",
                                    "/css/**", "/js/**", "/images/**")
                            .permitAll()

                            // Endpoints that require admin authentication
                            .requestMatchers("/adminDashboard", "/adminDashboard/**","/api/**")
                            .hasRole("DOMAIN_ADMIN")//endpoints that only DOMAIN_ADMIN role can access

                            .anyRequest().authenticated();// Everything else requires login, endpoints that require authentication
                });

                http.requestCache(RequestCacheConfigurer::disable);

                http.sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .expiredUrl("/adminLogin?expired=true")
                );

        return http.build();
    }
}

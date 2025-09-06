package com.unipi.gsimos.vistaseat.security;


import com.unipi.gsimos.vistaseat.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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

    // Authentication provider used by for admins
    @Bean
    public AuthenticationProvider adminAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Authentication provider used by for users
    @Bean
    public AuthenticationProvider userAuthenticationProvider() {
        UserOnlyAuthenticationProvider userAuthenticationProvider = new UserOnlyAuthenticationProvider();
        userAuthenticationProvider.setUserDetailsService(userDetailsService());
        userAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return userAuthenticationProvider;
    }

    // Default Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Filter chain for Admin actions
    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http, CustomAuthFailureHandler customAuthFailureHandler)
            throws Exception {

        http
                // Rules in this filterChain only apply to these admin-related endpoints.
                .securityMatcher("/adminLogin", "/adminLogout", "/adminDashboard", "/adminDashboard/**")

                // When someone logs in, check their credentials using adminAuthenticationProvider
                .authenticationManager(new ProviderManager(List.of(adminAuthenticationProvider())))

                .exceptionHandling(ex -> ex
                        // If the user isn’t logged in but tries to access an admin page → redirect them to /adminLogin
                        .authenticationEntryPoint((req,res,e) -> res.sendRedirect("/adminLogin"))
                        // If they are logged in but don’t have permission → send them to /error?status=403
                        .accessDeniedHandler((req,res,e) -> res.sendRedirect("/error?status=403"))
                )

                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(httpForm -> {
                            httpForm.loginPage("/adminLogin")
                                    .loginProcessingUrl("/adminLogin")
                                    .usernameParameter("email") // Instead of username, we’re using email
                                    .passwordParameter("password")
                                    .defaultSuccessUrl("/adminDashboard", true)
                                    .failureHandler(customAuthFailureHandler)
                                    .permitAll();// endpoints that are publicly accessible
                        })

                .logout(logout -> logout
                        .logoutUrl("/adminLogout")// the endpoint that performs logout
                        .logoutSuccessUrl("/home?logout=true") // redirect to home
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll() // Logout is publicly accessible.
                    )

                .authorizeHttpRequests(authorizeRequests -> authorizeRequests//creating exceptions for URLs we want to allow access

                        .requestMatchers("/adminLogin", "/adminLogout").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // Only users with the DOMAIN_ADMIN role can see the admin dashboard and its sub-pages
                        .requestMatchers("/adminDashboard", "/adminDashboard/**")
                        .hasRole("DOMAIN_ADMIN")
                        // For everything else in this security chain, you must be logged in (authenticated)
                        .anyRequest().authenticated());

                http.requestCache(RequestCacheConfigurer::disable);

                http.sessionManagement(session -> session
                        // Create a session only if needed
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        // Allow one active session per user
                        .maximumSessions(1)
                        .expiredUrl("/adminLogin?expired=true")
                );

        return http.build();
    }

    // Filter chain for user actions
    @Bean
    @Order(2)
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http, CustomAuthFailureHandler customAuthFailureHandler)
            throws Exception {

        http
                .authenticationManager(new ProviderManager(List.of(userAuthenticationProvider())))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, resp, e) -> resp.sendRedirect("/userLogin"))
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(form -> form
                        .loginPage("/userLogin")
                        .loginProcessingUrl("/userLogin")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        // Use saved request so users return to the page where they clicked "Connect"
                        .successHandler(userLoginSuccessHandler())
                        .failureHandler(customAuthFailureHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/userLogout")
                        .logoutSuccessUrl("/home?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .authorizeHttpRequests(auth -> auth
                        // block admins from even seeing the user login page
                        .requestMatchers(GET, "/userLogin")
                        .access((authz, ctx) -> new AuthorizationDecision(
                                authz.get().getAuthorities().stream()
                                        .noneMatch(a -> a.getAuthority().equals("ROLE_DOMAIN_ADMIN"))
                        ))


                        // Public pages & static
                        .requestMatchers("/home","/api/**","/userLogin","/userSignUp","/css/**","/js/**","/images/**",
                                "/error","/error/**").permitAll()
                        // User Registration endpoint
                        .requestMatchers("/api/users/register").permitAll()
                        // Auth-required user flows (adjust to your routes)
                        .requestMatchers("/userAccount","/userAccount/**")
                        .hasRole("REGISTERED")
                        // Everything else can be public, or tighten as you add features
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .expiredUrl("/userLogin?expired=true")
                );
        return http.build();
    }

    @Bean
    SavedRequestAwareAuthenticationSuccessHandler userLoginSuccessHandler() {
        var handler = new SavedRequestAwareAuthenticationSuccessHandler();
        handler.setTargetUrlParameter("redirectTo");
        handler.setDefaultTargetUrl("/home");
        handler.setAlwaysUseDefaultTargetUrl(false);
        handler.setUseReferer(true);
        return handler;
    }
}

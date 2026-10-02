package com.drivesense.config;

import com.drivesense.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username.trim().toLowerCase())
                .map(u -> {
                    java.util.List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority(u.getRole()));
                    if ("ROLE_CUSTOMER".equals(u.getRole())) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_RENTER"));
                    } else if ("ROLE_RENTER".equals(u.getRole())) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
                    }
                    return new org.springframework.security.core.userdetails.User(
                            u.getEmail(),
                            u.getPasswordHash(),
                            authorities
                    );
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/cars/**",
                    "/vibe-match/**",
                    "/compare/**",
                    "/how-it-works",
                    "/register",
                    "/owner/register",
                    "/login",
                    "/renter/login",
                    "/owner/login",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/h2-console/**",
                    "/api/cars/**",
                    "/api/price/**"
                ).permitAll()
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/owner/**", "/api/owner/**").hasAnyRole("OWNER", "ADMIN")
                .requestMatchers("/renter/**", "/book/**", "/my-bookings/**", "/profile/**", "/bookings/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    boolean isOwner = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
                    if (isAdmin) {
                        response.sendRedirect("/admin");
                    } else if (isOwner) {
                        response.sendRedirect("/owner/dashboard");
                    } else {
                        response.sendRedirect("/renter/dashboard");
                    }
                })
                .failureHandler((request, response, exception) -> {
                    String referer = request.getHeader("Referer");
                    if (referer != null && referer.contains("/owner/login")) {
                        response.sendRedirect("/owner/login?error=true");
                    } else {
                        response.sendRedirect("/login?error=true");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    String referer = request.getHeader("Referer");
                    if (referer != null && referer.contains("/owner")) {
                        response.sendRedirect("/owner/login?logout=true");
                    } else {
                        response.sendRedirect("/login?logout=true");
                    }
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/api/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }
}

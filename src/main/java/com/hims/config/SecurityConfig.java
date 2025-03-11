package com.hims.config;

import com.hims.jwt.JwtAuthenticationEntryPoint;
import com.hims.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint point;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter filter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(AUTH_WHITELIST).permitAll()  // Swagger paths
                        .requestMatchers("/authController/login").permitAll()
                        .requestMatchers("/authController/getUsersRole/{userName}").permitAll()
                        .requestMatchers("/apiTest/getTest").permitAll()
                        .requestMatchers("/users/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/flight/**").permitAll()
                        .requestMatchers("/app/**").permitAll()

                        .requestMatchers("/masterController/searchAirport/{query}").permitAll()
                        .requestMatchers("/masterController/frequentAirport").permitAll()
                        .requestMatchers("/authController/getEmpName/{empCode}").permitAll()
                        .requestMatchers("/authController/create-first-users").permitAll()
                        .requestMatchers("/fileController/upload").permitAll()
                        .requestMatchers("/gender/**").permitAll()
                        .requestMatchers("/relation/**").permitAll()
                        .requestMatchers("/marital-status/**").permitAll()
                        .requestMatchers("/religion/**").permitAll()
                        .requestMatchers("/district/**").permitAll()
                        .requestMatchers("/state/**").permitAll()
                        .requestMatchers("/country/**").permitAll()
                        .requestMatchers("/hospital/**").permitAll()
                        .requestMatchers("/department/**").permitAll()
                        .requestMatchers("/applications/**").permitAll()
                        .requestMatchers("/mas-applications/**").permitAll()
                        .requestMatchers("/mas-templates/**").permitAll()
                        .requestMatchers("/template-applications/**").permitAll()
                        .requestMatchers("/department-type/**").permitAll()
                        .requestMatchers("/opd-session/**").permitAll()
                        .requestMatchers("/relation/**").permitAll()
                        .requestMatchers("/blood-group/**").permitAll()
                        .requestMatchers("/roles/**").permitAll()
                        .requestMatchers("/religion/**").permitAll()
                        .requestMatchers("/identification-types/**").permitAll()
                        .requestMatchers("/v3/**", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(point))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static final String[] AUTH_WHITELIST = {
            // Swagger paths
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-resources/**",
            "/swagger-resources/configuration/security"
    };

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Define the PasswordEncoder bean
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder) throws Exception {
        return builder.getAuthenticationManager();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

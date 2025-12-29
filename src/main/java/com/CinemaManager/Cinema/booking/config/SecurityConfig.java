package com.CinemaManager.Cinema.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Разрешаем доступ к публичным эндпоинтам без аутентификации
                        .requestMatchers(
                                "/",                            // Главная страница
                                "/api/test",                    // Тестовый эндпоинт
                                "/api/auth/**",                 // Аутентификация и регистрация
                                "/api/movies/**",               // Фильмы (публичный доступ)
                                "/api/sessions/**",             // Сеансы (публичный доступ)
                                "/api/halls/**",                // Залы (публичный доступ)
                                "/api/tickets/**",              // Билеты (для тестирования)
                                "/h2-console/**",               // H2 консоль
                                "/swagger-ui/**",               // Swagger UI
                                "/v3/api-docs/**",              // OpenAPI документация
                                "/swagger-ui.html",             // Swagger HTML
                                "/favicon.ico"                  // Иконка
                        ).permitAll()
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")                // Кастомная страница логина
                        .permitAll()
                        .defaultSuccessUrl("/", true)       // Перенаправление после логина
                )
                .logout(logout -> logout
                        .permitAll()
                        .logoutSuccessUrl("/")              // После выхода на главную
                )
                .csrf(csrf -> csrf
                        // ОТКЛЮЧАЕМ CSRF для всех API-эндпоинтов и H2 консоли
                        // Это нужно для REST API и тестирования через curl/Postman
                        .ignoringRequestMatchers(
                                "/api/**",                      // Все API эндпоинты
                                "/h2-console/**",               // H2 Console
                                "/swagger-ui/**",               // Swagger
                                "/v3/api-docs/**"               // OpenAPI
                        )
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // Для H2 console
                );

        return http.build();
    }
}
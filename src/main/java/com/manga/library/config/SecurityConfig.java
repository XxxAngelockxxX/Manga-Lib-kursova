package com.manga.library.config;

import com.manga.library.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Дозволяє використовувати @PreAuthorize("hasRole('ADMIN')") в контролерах
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    // Використовуємо конструктор для ін'єкції сервісу
    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService) {
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults()) // <--- ДОДАЙ ОСЬ СЮДИ
                .csrf(csrf -> csrf.disable()) // Вимикаємо для REST API
                .authorizeHttpRequests(auth -> auth
                        // 1. Дозволяємо перегляд (GET) для всіх ендпоінтів API
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()

                        // 2. Дозволяємо Swagger, логін та головну сторінку всім
                        .requestMatchers("/", "/login", "/error", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 3. Будь-які інші запити (POST, PUT, DELETE) вимагають входу в систему
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        // Підключаємо наш сервіс, який реєструє юзера в БД і дістає його роль
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                );

        return http.build();
    }
}
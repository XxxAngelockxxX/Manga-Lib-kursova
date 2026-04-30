package com.manga.library.service;

import com.manga.library.model.Role;
import com.manga.library.model.User;
import com.manga.library.repository.RoleRepository;
import com.manga.library.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            // Спочатку дістаємо ролі з бази даних
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Помилка: ROLE_ADMIN не знайдено"));
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Помилка: ROLE_USER не знайдено"));

            // Тепер створюємо користувача
            User newUser = User.builder()
                    .email(email)
                    .username(name != null ? name : email)
                    .role("st8504762@stud.duikt.edu.ua".equals(email) ? adminRole : userRole)
                    .passwordHash("OAUTH2")
                    .isActive(true)
                    .build();

            return userRepository.save(newUser);
        });

        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority(user.getRole().getName())
        );

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
    }
}
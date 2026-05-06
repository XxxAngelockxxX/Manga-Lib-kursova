package com.manga.library.controller;

import com.manga.library.model.Role;
import com.manga.library.model.User;
import com.manga.library.repository.RoleRepository;
import com.manga.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User targetUser;

    @BeforeEach
    void setUp() {
        // Створюємо базові ролі для тесту
        Role userRole = roleRepository.save(Role.builder().name("ROLE_USER").build());
        roleRepository.save(Role.builder().name("ROLE_AUTHOR").build());

        // Створюємо звичайного користувача, якому потім будемо змінювати роль
        targetUser = userRepository.save(User.builder()
                .username("simple_reader")
                .email("reader@test.com")
                .role(userRole)
                .build());
    }

    // ==========================================
    // ПОЗИТИВНІ СЦЕНАРІЇ
    // ==========================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldGetAllUsersWhenUserIsAdmin() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(1))); // Очікуємо хоча б одного користувача (нашого targetUser)
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateUserRoleWhenUserIsAdmin() throws Exception {
        // Адмін змінює роль користувача на AUTHOR
        mockMvc.perform(put("/api/users/" + targetUser.getId() + "/role")
                        .param("roleName", "ROLE_AUTHOR"))
                .andExpect(status().isOk());

        // Перевіряємо в базі даних, чи дійсно роль змінилася
        User updatedUser = userRepository.findById(targetUser.getId()).orElseThrow();
        assertEquals("ROLE_AUTHOR", updatedUser.getRole().getName());
    }

    // ==========================================
    // НЕГАТИВНІ СЦЕНАРІЇ
    // ==========================================

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnForbiddenWhenSimpleUserTriesToGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden()); // Звичайний користувач не має бачити список усіх user
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void shouldReturnForbiddenWhenAuthorTriesToChangeRole() throws Exception {
        // Автор намагається зробити когось адміном
        mockMvc.perform(put("/api/users/" + targetUser.getId() + "/role")
                        .param("roleName", "ROLE_ADMIN"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnRedirectWhenGuestTriesToAccessUsers() throws Exception {
        // Гість (без авторизації) намагається отримати список
        mockMvc.perform(get("/api/users"))
                .andExpect(status().is3xxRedirection()) // Очікуємо 302 редирект
                .andExpect(redirectedUrlPattern("**/oauth2/authorization/google")); // Перевіряємо, що перенаправляє саме на логін
    }

}
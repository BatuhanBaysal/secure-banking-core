package com.batuhan.banking_service.controller.user;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.config.SecurityConfig;
import com.batuhan.banking_service.controller.BaseControllerTest;
import com.batuhan.banking_service.controller.UserController;
import com.batuhan.banking_service.dto.request.UserCreateRequest;
import com.batuhan.banking_service.dto.request.UserUpdateRequest;
import com.batuhan.banking_service.dto.response.UserResponse;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the UserController.
 * Verifies user registration, profile updates, and retrieval operations.
 * Tests role-based access control (RBAC) and profile ownership validation.
 */
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("User Controller - API Integration Tests")
class UserControllerTest extends BaseControllerTest {

    private static final String BASE_URL = "/api/v1/users";

    @BeforeEach
    void setupCommonMocks() {
        // Given
        RateLimiter mockRateLimiter = mock(RateLimiter.class);

        // When
        lenient().when(rateLimiterRegistry.rateLimiter(anyString())).thenReturn(mockRateLimiter);
        lenient().when(mockRateLimiter.acquirePermission()).thenReturn(true);
        lenient().when(bankingBusinessValidator.isOwner(anyString())).thenReturn(true);
    }

    @Nested
    @DisplayName("1. User Registration (POST)")
    class RegistrationTests {

        @Test
        @DisplayName("Success: Public user can register")
        void createUser_ValidRequest_ReturnsCreated() throws Exception {
            // Given
            UserCreateRequest request = TestDataFactory.createUserCreateRequest();
            UserResponse response = TestDataFactory.createUserResponse("CUS101");

            // When
            when(userService.createUser(any())).thenReturn(response);

            // Then
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.data.customerNumber").value("CUS101"));
        }
    }

    @Nested
    @DisplayName("2. User Retrieval (GET)")
    class RetrievalTests {

        @Test
        @DisplayName("Success: Admin can retrieve any user profile")
        void getUserByCustomerNumber_AsAdmin_ReturnsOk() throws Exception {
            // Given
            String customerNo = "CUS101";
            UserResponse response = TestDataFactory.createUserResponse(customerNo);

            // When
            when(userService.getUserByCustomerNumber(customerNo)).thenReturn(response);

            // Then
            mockMvc.perform(get(BASE_URL + "/{customerNumber}", customerNo)
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.customerNumber").value(customerNo));
        }

        @Test
        @DisplayName("Success: Admin can list all users with pagination")
        void getAllUsers_AsAdmin_ReturnsPaginatedList() throws Exception {
            // Given
            UserResponse userResponse = TestDataFactory.createUserResponse("CUS-LIST");
            Page<UserResponse> page = new PageImpl<>(List.of(userResponse));

            // When
            when(userService.getAllUsers(any())).thenReturn(page);

            // Then
            mockMvc.perform(get(BASE_URL)
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].customerNumber").value("CUS-LIST"));
        }

        @Test
        @DisplayName("Failure: Regular user cannot list all users (403)")
        void getAllUsers_AsUser_ReturnsForbidden() throws Exception {
            // Then
            mockMvc.perform(get(BASE_URL)
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("3. User Update (PUT)")
    class UpdateTests {

        @Test
        @DisplayName("Success: Profile owner can update their own details")
        void updateUser_AsOwner_ReturnsOk() throws Exception {
            // Given
            String customerNo = "OWNER-1";
            UserUpdateRequest request = TestDataFactory.createUserUpdateRequest();
            UserResponse response = TestDataFactory.createUserResponse(customerNo);

            // When
            when(bankingBusinessValidator.isOwner(customerNo)).thenReturn(true);
            when(userService.updateUser(eq(customerNo), any())).thenReturn(response);

            // Then
            mockMvc.perform(put(BASE_URL + "/{customerNumber}", customerNo)
                            .with(csrf())
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Failure: User cannot update another person's profile (403)")
        void updateUser_NotOwner_ReturnsForbidden() throws Exception {
            // Given
            String customerNo = "OTHER-1";
            UserUpdateRequest request = TestDataFactory.createUserUpdateRequest();

            // When
            when(bankingBusinessValidator.isOwner(customerNo)).thenReturn(false);

            // Then
            mockMvc.perform(put(BASE_URL + "/{customerNumber}", customerNo)
                            .with(csrf())
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("4. User Deactivation (DELETE)")
    class DeleteTests {

        @Test
        @DisplayName("Success: Admin can deactivate any user")
        void deleteUser_AsAdmin_ReturnsOk() throws Exception {
            // Given
            String customerNo = "CUS-TO-DELETE";

            // When
            doNothing().when(userService).deleteUser(customerNo);

            // Then
            mockMvc.perform(delete(BASE_URL + "/{customerNumber}", customerNo)
                            .with(csrf())
                            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                    .andExpect(status().isOk());
        }
    }
}
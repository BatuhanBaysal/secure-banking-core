package com.batuhan.banking_service.service.user;

import com.batuhan.banking_service.TestDataFactory;
import com.batuhan.banking_service.dto.request.UserCreateRequest;
import com.batuhan.banking_service.dto.request.UserUpdateRequest;
import com.batuhan.banking_service.dto.response.UserResponse;
import com.batuhan.banking_service.entity.UserEntity;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.service.BaseServiceTest;
import com.batuhan.banking_service.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl.
 * Validates user registration, profile management, and deactivation logic.
 * Ensures strict enforcement of business rules such as email uniqueness,
 * TCKN validation, profile ownership, and soft-delete consistency.
 */
@DisplayName("User Service Comprehensive Unit Tests - Modernized")
class UserServiceTest extends BaseServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("1. Registration Logic (Create User)")
    class RegistrationTests {

        @Test
        @DisplayName("Success: Return response when request is valid and credentials unique")
        void shouldCreateUser_WhenValid() {
            // Given
            UserCreateRequest request = TestDataFactory.createUserCreateRequest();
            UserEntity userEntity = TestDataFactory.createTestUser();
            UserResponse expectedResponse = TestDataFactory.createUserResponse(userEntity.getCustomerNumber());

            // When
            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(userRepository.existsByTckn(request.tckn())).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(userEntity);
            when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
            when(userMapper.toResponse(any(UserEntity.class))).thenReturn(expectedResponse);

            UserResponse result = userService.createUser(request);

            // Then
            assertAll("User Creation Assertions",
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.email()).isEqualTo(request.email()),
                    () -> verify(userRepository).save(any(UserEntity.class))
            );
        }

        @Test
        @DisplayName("Failure: Throw Conflict when email already exists in system")
        void shouldThrowConflict_WhenEmailExists() {
            // Given
            UserCreateRequest request = TestDataFactory.createUserCreateRequest();

            // When
            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            // Then
            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(BankingServiceException.class)
                    .matches(ex -> ((BankingServiceException) ex).getStatus() == HttpStatus.CONFLICT);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("2. Retrieval Logic (Get & List)")
    class RetrievalTests {

        @Test
        @DisplayName("Success: Retrieve mapped user by valid customer number")
        void shouldGetUser_ByCustomerNumber() {
            // Given
            String customerNo = "1234567890";
            UserEntity user = TestDataFactory.createTestUser();
            user.setCustomerNumber(customerNo);
            UserResponse mockResponse = TestDataFactory.createUserResponse(customerNo);

            // When
            when(userRepository.findByCustomerNumber(customerNo)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(mockResponse);

            UserResponse result = userService.getUserByCustomerNumber(customerNo);

            // Then
            assertAll("Retrieval Verifications",
                    () -> assertThat(result.customerNumber()).isEqualTo(customerNo),
                    () -> verify(bankingBusinessValidator).validateOwnership(any(UserEntity.class))
            );
        }

        @Test
        @DisplayName("Success: Map all user entities to response during pagination")
        void shouldMapAllUsers_ToResponse() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            List<UserEntity> users = List.of(TestDataFactory.createTestUser(), TestDataFactory.createTestUser());
            Page<UserEntity> userPage = new PageImpl<>(users);

            // When
            when(userRepository.findAll(pageable)).thenReturn(userPage);
            userService.getAllUsers(pageable);

            // Then
            verify(userMapper, times(2)).toResponse(any(UserEntity.class));
        }
    }

    @Nested
    @DisplayName("3. Update & Consistency Logic")
    class UpdateTests {

        @Test
        @DisplayName("Success: Skip uniqueness check when email remains unchanged")
        void shouldNotCheckUniqueness_WhenEmailIsSame() {
            // Given
            String customerNo = "123";
            UserEntity user = TestDataFactory.createTestUser();
            user.setEmail("batuhan@test.com");
            UserUpdateRequest request = TestDataFactory.createUserUpdateRequest();
            user.setEmail(request.email());

            // When
            when(userRepository.findByCustomerNumber(customerNo)).thenReturn(Optional.of(user));
            doNothing().when(userMapper).updateEntityFromDto(any(), any());
            when(userRepository.save(any())).thenReturn(user);

            userService.updateUser(customerNo, request);

            // Then
            assertAll("Update Constraints",
                    () -> verify(userRepository, never()).existsByEmail(anyString()),
                    () -> verify(userRepository).save(any(UserEntity.class))
            );
        }
    }

    @Nested
    @DisplayName("4. Security & Deactivation Logic")
    class SecurityTests {

        @Test
        @DisplayName("Failure: Block deactivation when user is not the profile owner")
        void shouldPreventAction_WhenNotOwner() {
            // Given
            String customerNo = "123";
            UserEntity mockUser = TestDataFactory.createTestUser();

            // When
            when(userRepository.findByCustomerNumber(customerNo))
                    .thenReturn(Optional.of(mockUser));

            doThrow(new BankingServiceException("Forbidden", HttpStatus.FORBIDDEN))
                    .when(bankingBusinessValidator).validateOwnership(any(UserEntity.class));

            // Then
            assertThatThrownBy(() -> userService.deleteUser(customerNo))
                    .isInstanceOf(BankingServiceException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("Success: Set active flag to false during soft delete")
        void shouldPerformSoftDelete_Successfully() {
            // Given
            String customerNo = "12345";
            UserEntity user = TestDataFactory.createTestUser();
            user.setActive(true);

            // When
            when(userRepository.findByCustomerNumber(customerNo)).thenReturn(Optional.of(user));
            when(userRepository.save(any())).thenReturn(user);

            userService.deleteUser(customerNo);

            // Then
            assertAll("Soft Delete Verification",
                    () -> assertThat(user.isActive()).isFalse(),
                    () -> verify(userRepository).save(user)
            );
        }
    }
}
package com.example.bankcards.service;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDto testUserDto;
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setUsername(USERNAME);
        testUser.setEmail(EMAIL);
        testUser.setRole(Role.USER);
        testUser.setEnabled(true);

        testUserDto = new UserDto();
        testUserDto.setId(USER_ID);
        testUserDto.setUsername(USERNAME);
        testUserDto.setEmail(EMAIL);
        testUserDto.setRole(Role.USER);
        testUserDto.setEnabled(true);
    }

    @Nested
    @DisplayName("Load User By Username Tests")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should load user by username successfully")
        void loadUserByUsername_Success() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));

            // When
            UserDetails result = userService.loadUserByUsername(USERNAME);

            // Then
            assertNotNull(result);
            assertEquals(USERNAME, result.getUsername());
            verify(userRepository).findByUsername(USERNAME);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void loadUserByUsername_NotFound() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UsernameNotFoundException.class, () -> 
                userService.loadUserByUsername(USERNAME));
            verify(userRepository).findByUsername(USERNAME);
        }
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user successfully")
        void createUser_Success() {
            // Given
            String encodedPassword = "encodedPassword123";
            when(passwordEncoder.encode(PASSWORD)).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDto result = userService.createUser(testUserDto, PASSWORD);

            // Then
            assertNotNull(result);
            assertEquals(USERNAME, result.getUsername());
            assertEquals(EMAIL, result.getEmail());
            assertEquals(Role.USER, result.getRole());
            assertTrue(result.isEnabled());
            verify(passwordEncoder).encode(PASSWORD);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should create user with default role when role is null")
        void createUser_WithDefaultRole() {
            // Given
            testUserDto.setRole(null);
            String encodedPassword = "encodedPassword123";
            when(passwordEncoder.encode(PASSWORD)).thenReturn(encodedPassword);
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDto result = userService.createUser(testUserDto, PASSWORD);

            // Then
            assertNotNull(result);
            assertEquals(Role.USER, result.getRole());
            verify(passwordEncoder).encode(PASSWORD);
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Get User Tests")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by ID successfully")
        void getUserById_Success() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // When
            UserDto result = userService.getUserById(USER_ID);

            // Then
            assertNotNull(result);
            assertEquals(USER_ID, result.getId());
            assertEquals(USERNAME, result.getUsername());
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found by ID")
        void getUserById_NotFound() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () -> 
                userService.getUserById(USER_ID));
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("Should get user by username successfully")
        void getUserByUsername_Success() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(testUser));

            // When
            UserDto result = userService.getUserByUsername(USERNAME);

            // Then
            assertNotNull(result);
            assertEquals(USERNAME, result.getUsername());
            verify(userRepository).findByUsername(USERNAME);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user not found by username")
        void getUserByUsername_NotFound() {
            // Given
            when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () -> 
                userService.getUserByUsername(USERNAME));
            verify(userRepository).findByUsername(USERNAME);
        }
    }

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should get all users successfully")
        void getAllUsers_Success() {
            // Given
            when(userRepository.findAll()).thenReturn(List.of(testUser));

            // When
            List<UserDto> result = userService.getAllUsers();

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(USERNAME, result.get(0).getUsername());
            verify(userRepository).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void getAllUsers_EmptyList() {
            // Given
            when(userRepository.findAll()).thenReturn(List.of());

            // When
            List<UserDto> result = userService.getAllUsers();

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(userRepository).findAll();
        }
    }

    @Nested
    @DisplayName("Update User Tests")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully")
        void updateUser_Success() {
            // Given
            UserDto updateDto = new UserDto();
            updateDto.setUsername("updateduser");
            updateDto.setEmail("updated@example.com");
            updateDto.setRole(Role.ADMIN);
            updateDto.setEnabled(false);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);

            // When
            UserDto result = userService.updateUser(USER_ID, updateDto);

            // Then
            assertNotNull(result);
            verify(userRepository).findById(USER_ID);
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when updating non-existent user")
        void updateUser_NotFound() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () -> 
                userService.updateUser(USER_ID, testUserDto));
            verify(userRepository).findById(USER_ID);
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void deleteUser_Success() {
            // Given
            when(userRepository.existsById(USER_ID)).thenReturn(true);

            // When
            userService.deleteUser(USER_ID);

            // Then
            verify(userRepository).existsById(USER_ID);
            verify(userRepository).deleteById(USER_ID);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
        void deleteUser_NotFound() {
            // Given
            when(userRepository.existsById(USER_ID)).thenReturn(false);

            // When & Then
            assertThrows(UserNotFoundException.class, () -> 
                userService.deleteUser(USER_ID));
            verify(userRepository).existsById(USER_ID);
            verify(userRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityMethodsTests {

        @Test
        @DisplayName("Should get user entity by ID successfully")
        void getUserEntityById_Success() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

            // When
            User result = userService.getUserEntityById(USER_ID);

            // Then
            assertNotNull(result);
            assertEquals(USER_ID, result.getId());
            assertEquals(USERNAME, result.getUsername());
            verify(userRepository).findById(USER_ID);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when getting non-existent user entity")
        void getUserEntityById_NotFound() {
            // Given
            when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(UserNotFoundException.class, () -> 
                userService.getUserEntityById(USER_ID));
            verify(userRepository).findById(USER_ID);
        }
    }
}

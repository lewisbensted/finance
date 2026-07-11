package finance.slice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import finance.controllers.UserController;
import finance.dtos.LoginDTO;
import finance.dtos.PasswordDTO;
import finance.dtos.RegisterDTO;
import finance.entities.User;
import finance.exceptions.AuthenticationException;
import finance.exceptions.RegistrationException;
import finance.services.UserService;
import finance.session.SessionUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static finance.dtos.ErrorCode.INVALID_CREDENTIALS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    User testUser = mock(User.class);

    @BeforeEach
    void setUp() {
        when(testUser.getId()).thenReturn(1L);
        when(testUser.getUsername()).thenReturn("testuser");
    }


    SessionUser testSessionUser = new SessionUser(1L, "testuser");

    @Nested
    class RegisterControllerTests {
        @Test
        void test201Success() throws Exception {
            RegisterDTO request = new RegisterDTO(
                    "testuser",
                    "testuser@test.com",
                    "test",
                    "user",
                    "password123!",
                    "password123!"
            );
            when(userService.register(any())).thenReturn(testUser);

            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("testuser"));
        }

        @Test
        void test403AlreadyRegistered() throws Exception {
            RegisterDTO request = new RegisterDTO(
                    "testuser",
                    "testuser@test.com",
                    "test",
                    "user",
                    "password123!",
                    "password123!"
            );
            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("Cannot register while logged in"));
        }

        @Test
        void test400Invalid() throws Exception {
            RegisterDTO request = new RegisterDTO(
                    "te",
                    "testuser.com",
                    "test1",
                    "user",
                    "password123",
                    null
            );
            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Validation error(s)"))
                    .andExpect(jsonPath("$.fields.firstName").value("Invalid first name format"))
                    .andExpect(jsonPath("$.fields.username").value("Username must be between 3 and 20 characters"))
                    .andExpect(jsonPath("$.fields.email").value("Invalid email address"))
                    .andExpect(jsonPath("$.fields.password").value("Password must contain a number, letter and special character, without spaces"))
                    .andExpect(jsonPath("$.fields.passwordRepeat").value("Password confirmation is required"));
        }

        @Test
        void test400NullBody() throws Exception {
            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test422UserTaken() throws Exception {
            RegisterDTO request = new RegisterDTO(
                    "testuser",
                    "testuser@test.com",
                    "test",
                    "user",
                    "password123!",
                    "password123!"
            );
            when(userService.register(any())).thenThrow(new RegistrationException("User taken"));
            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("REGISTRATION_FAILED"))
                    .andExpect(jsonPath("$.message").value("User taken"));
        }
    }

    @Nested
    class LoginControllerTests {
        @Test
        void test400MalformedJSON() throws Exception {
            String badJson = """
                    {
                        "username": "testuser",
                        "password": "testpassword",
                    }
                    """;

            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(badJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test200Success() throws Exception {
            LoginDTO request = new LoginDTO(
                    "testuser",
                    "password123!"
            );
            when(userService.login(any())).thenReturn(testUser);

            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(request().sessionAttribute("USER_SESSION", testSessionUser));
        }

        @Test
        void test403AlreadyLoggedIn() throws Exception {
            LoginDTO request = new LoginDTO(
                    "testuser1",
                    "password123!"
            );
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("Already logged in"));
        }


        @Test
        void test400NullBody() throws Exception {
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test401IncorrectCredentials() throws Exception {
            LoginDTO request = new LoginDTO(
                    "testuser",
                    "password123!"
            );
            when(userService.login(any())).thenThrow(new AuthenticationException(INVALID_CREDENTIALS, "Invalid username"));
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                    .andExpect(jsonPath("$.message").value("Invalid username"));
        }

        @Test
        void test400Invalid() throws Exception {
            LoginDTO request = new LoginDTO(
                    "",
                    null
            );
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Validation error(s)"))
                    .andExpect(jsonPath("$.fields.username").value("Username is required"))
                    .andExpect(jsonPath("$.fields.password").value("Password is required"));

        }
    }

    @Nested
    class LogoutControllerTests {
        @Test
        void test200Success() throws Exception {

            when(userService.login(any())).thenReturn(testUser);

            mockMvc.perform(post("/api/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Logged out"))
                    .andExpect(request().sessionAttributeDoesNotExist("USER_SESSION"));
        }

        @Test
        void test403NotLoggedIn() throws Exception {
            mockMvc.perform(post("/api/logout")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Logged out"));
        }
    }

    @Nested
    class PasswordControllerTests {
        PasswordDTO newTestPassword = new PasswordDTO("password", "newpassword123!");

        @Test
        void test400BadJson() throws Exception {
            String badJson = """
                    {
                        "password": "password",
                        "newPassword": "newpassword",
                    }
                    """;
            mockMvc.perform(put("/api/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser)
                            .content(badJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test401Unauthenticated() throws Exception {
            mockMvc.perform(put("/api/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newTestPassword)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                    .andExpect(jsonPath("$.message").value("Not logged in"));
        }

        @Test
        void test400ChangePasswordFails() throws Exception {
            doThrow(new AuthenticationException(INVALID_CREDENTIALS, "Incorrect password"))
                    .when(userService)
                    .changePassword(anyLong(), any());
            mockMvc.perform(put("/api/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser)
                            .content(objectMapper.writeValueAsString(newTestPassword)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
                    .andExpect(jsonPath("$.message").value("Incorrect password"));
        }

        @Test
        void test400Invalid() throws Exception {
            mockMvc.perform(put("/api/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser)
                            .content(objectMapper.writeValueAsString(new PasswordDTO("", "newpassword123"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Validation error(s)"))
                    .andExpect(jsonPath("$.fields.password").value("Password is required"))
                    .andExpect(jsonPath("$.fields.newPassword").value("New password must contain a number, letter and special character, without spaces"));
        }

        @Test
        void test200Success() throws Exception {
            doNothing().when(userService).changePassword(anyLong(), any());
            mockMvc.perform(put("/api/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser)
                            .content(objectMapper.writeValueAsString(newTestPassword)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Password updated"));
        }
    }
}

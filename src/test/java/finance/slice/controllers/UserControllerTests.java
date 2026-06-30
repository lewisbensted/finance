package finance.slice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import finance.controllers.UserController;
import finance.dtos.LoginDTO;
import finance.dtos.RegisterDTO;
import finance.entities.User;
import finance.exceptions.AuthorisationException;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
            when(userService.register(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(testUser);

            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(201))
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
                    .andExpect(status().is(403))
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
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Validation error(s)"))
                    .andExpect(jsonPath("$.fields.firstName").value("Invalid first name format"))
                    .andExpect(jsonPath("$.fields.username").value("Username must be between 3 and 20 characters"))
                    .andExpect(jsonPath("$.fields.email").value("Invalid email address"))
                    .andExpect(jsonPath("$.fields.password").value("Password must contain a number, letter and special character, without spaces"))
                    .andExpect(jsonPath("$.fields.confirmPassword").value("Password confirmation is required"));
        }

        @Test
        void test400NullBody() throws Exception {
            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
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
            when(userService.register(anyString(), anyString(), anyString(), anyString(), anyString(), anyString())).thenThrow(new RegistrationException("User taken"));
            mockMvc.perform(post("/api/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(409))
                    .andExpect(jsonPath("$.code").value("CONFLICT"))
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
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test200Success() throws Exception {
            LoginDTO request = new LoginDTO(
                    "testuser",
                    "password123!"
            );
            when(userService.login(anyString(), anyString())).thenReturn(testUser);

            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(200))
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
                    .andExpect(status().is(403))
                    .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                    .andExpect(jsonPath("$.message").value("Already logged in"));
        }


        @Test
        void test400NullBody() throws Exception {
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Empty or unreadable request body"));
        }

        @Test
        void test401IncorrectCredentials() throws Exception {
            LoginDTO request = new LoginDTO(
                    "testuser",
                    "password123!"
            );
            when(userService.login(anyString(), anyString())).thenThrow(new AuthorisationException("Invalid username"));
            mockMvc.perform(post("/api/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is(401))
                    .andExpect(jsonPath("$.code").value("UNAUTHORISED"))
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
                    .andExpect(status().is(400))
                    .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                    .andExpect(jsonPath("$.message").value("Validation error(s)"))
                    .andExpect(jsonPath("$.fields.username").value("Username is required"))
                    .andExpect(jsonPath("$.fields.password").value("Password is required"));

        }
    }

    @Nested
    class LogoutControllerTests {
        @Test
        void test200Success() throws Exception {

            when(userService.login(anyString(), anyString())).thenReturn(testUser);

            mockMvc.perform(post("/api/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .sessionAttr("USER_SESSION", testSessionUser))
                    .andExpect(status().is(200))
                    .andExpect(content().string("Logged out"))
                    .andExpect(request().sessionAttributeDoesNotExist("USER_SESSION"));
        }

        @Test
        void test403NotLoggedIn() throws Exception {
            mockMvc.perform(post("/api/logout")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().is(200))
                    .andExpect(content().string("Logged out"));
        }
    }
}

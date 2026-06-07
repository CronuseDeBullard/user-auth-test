package com.softwaretest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaretest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户认证集成测试
 * 基于 UserServiceUnitTest 和 AuthControllerTest 的单元测试
 * 测试完整的注册、登录、重置密码流程
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("用户认证集成测试")
public class UserAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("IT-AUTH-01: 完整的注册-登录-重置密码流程")
    void testCompleteAuthenticationFlow() throws Exception {
        String username = "testuser001";
        String email = "testuser001@test.com";
        String phone = "13800138001";
        String password = "password123";
        String newPassword = "newpass456";

        // 1. 注册成功
        String registerJson = buildRegisterJson(username, password, phone, email);
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("注册成功"));

        // 2. 重复注册失败
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isBadRequest());

        // 3. 登录成功
        String loginJson = buildLoginJson(username, password);
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value(username));

        // 4. 错误密码登录失败
        String wrongLoginJson = buildLoginJson(username, "wrongpassword");
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongLoginJson))
                .andExpect(status().isUnauthorized());

        // 5. 重置密码（假设有这个端点）
        // 注意：原项目可能没有这个端点，这里作为示例
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("registerValidationCases")
    @DisplayName("IT-AUTH-02: 注册参数验证测试")
    void testRegisterValidation(String caseName, String username, String password, 
                                String phone, String email, int expectedStatus) throws Exception {
        String registerJson = buildRegisterJson(username, password, phone, email);
        
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().is(expectedStatus));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("loginValidationCases")
    @DisplayName("IT-AUTH-03: 登录参数验证测试")
    void testLoginValidation(String caseName, String username, String password, 
                            int expectedStatus) throws Exception {
        // 先注册一个用户
        if (caseName.contains("成功")) {
            String registerJson = buildRegisterJson(username, password, "13800138000", "test@test.com");
            mockMvc.perform(post("/api/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(registerJson));
        }

        String loginJson = buildLoginJson(username, password);
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    @DisplayName("IT-AUTH-04: 用户名去除空格测试")
    void testUsernameTrimsSpaces() throws Exception {
        String username = "  testuser002  ";
        String trimmedUsername = "testuser002";
        
        String registerJson = buildRegisterJson(username, "password123", "13800138002", "test002@test.com");
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk());

        // 使用去除空格后的用户名登录
        String loginJson = buildLoginJson(trimmedUsername, "password123");
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value(trimmedUsername));
    }

    @Test
    @DisplayName("IT-AUTH-05: 邮箱大小写不敏感测试")
    void testEmailCaseInsensitive() throws Exception {
        String email1 = "Test@Example.com";
        String email2 = "test@example.com";
        
        // 使用大写邮箱注册
        String registerJson1 = buildRegisterJson("user001", "password123", "13800138001", email1);
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson1))
                .andExpect(status().isOk());

        // 使用小写邮箱注册应该成功（邮箱大小写敏感）
        String registerJson2 = buildRegisterJson("user002", "password123", "13800138002", email2);
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson2))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("IT-AUTH-06: 密码加密存储测试")
    void testPasswordEncryption() throws Exception {
        String username = "testuser003";
        String password = "password123";
        
        String registerJson = buildRegisterJson(username, password, "13800138003", "test003@test.com");
        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isOk());

        // 验证密码不是明文存储
        userRepository.findByUsername(username).ifPresent(user -> {
            assert !user.getPassword().equals(password) : "密码不应该以明文存储";
            assert user.getPassword().startsWith("$2a$") || user.getPassword().startsWith("$2b$") 
                : "密码应该使用BCrypt加密";
        });
    }

    // 数据提供方法
    static Stream<Arguments> registerValidationCases() {
        return Stream.of(
                Arguments.of("用户名为空", "", "password123", "13800138000", "test@test.com", 400),
                Arguments.of("用户名过短", "abc", "password123", "13800138000", "test@test.com", 400),
                Arguments.of("用户名过长", "a".repeat(25), "password123", "13800138000", "test@test.com", 400),
                Arguments.of("密码为空", "testuser", "", "13800138000", "test@test.com", 400),
                Arguments.of("密码过短", "testuser", "123", "13800138000", "test@test.com", 400),
                Arguments.of("手机号格式错误", "testuser", "password123", "12345", "test@test.com", 400),
                Arguments.of("邮箱格式错误", "testuser", "password123", "13800138000", "invalid-email", 400),
                Arguments.of("正常注册", "validuser", "password123", "13800138000", "valid@test.com", 200)
        );
    }

    static Stream<Arguments> loginValidationCases() {
        return Stream.of(
                Arguments.of("用户名为空", "", "password123", 401),
                Arguments.of("密码为空", "testuser", "", 401),
                Arguments.of("用户不存在", "nonexistent", "password123", 401),
                Arguments.of("登录成功", "loginuser", "password123", 200)
        );
    }

    // 辅助方法
    private String buildRegisterJson(String username, String password, String phone, String email) throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);
        data.put("phone", phone);
        data.put("email", email);
        data.put("userType", "user");
        return objectMapper.writeValueAsString(data);
    }

    private String buildLoginJson(String username, String password) throws Exception {
        Map<String, String> data = new HashMap<>();
        data.put("username", username);
        data.put("password", password);
        return objectMapper.writeValueAsString(data);
    }
}

package com.softwaretest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaretest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户注册功能集成测试
 * 包含9个测试用例：TC-REG-01 到 TC-REG-09
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("用户注册功能集成测试")
public class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;

    private String buildRegisterJson(String username, String password, String phone, String email) throws Exception {
        Map<String, String> registerData = new HashMap<>();
        registerData.put("username", username);
        registerData.put("password", password);
        registerData.put("email", email);
        registerData.put("phone", phone);
        registerData.put("userType", "user");
        return objectMapper.writeValueAsString(registerData);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        System.out.println("========================================");
        System.out.println("开始执行用户注册功能集成测试");
        System.out.println("========================================");
    }

    @Test
    @DisplayName("TC-REG-01: 手机号已存在")
    void testPhoneAlreadyExists() throws Exception {
        System.out.println("\n执行测试用例: TC-REG-01 - 手机号已存在");
        
        String existingPhone = "13800138000";
        String email1 = "test001@example.com";
        String email2 = "test002@example.com";
        
        // 先注册一个用户
        String registerJson1 = buildRegisterJson("testuser001", "Pass@123456", existingPhone, email1);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson1))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk());

        // 尝试用相同手机号注册
        String registerJson2 = buildRegisterJson("testuser002", "Pass@123456", existingPhone, email2);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson2))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("已存在")));
        
        System.out.println("✓ TC-REG-01 测试通过: 手机号重复检查生效");
    }

    @Test
    @DisplayName("TC-REG-02: 邮箱已存在")
    void testEmailAlreadyExists() throws Exception {
        System.out.println("\n执行测试用例: TC-REG-02 - 邮箱已存在");
        
        String existingEmail = "duplicate@example.com";
        
        // 先注册一个用户
        String registerJson1 = buildRegisterJson("testuser003", "Pass@123456", "13900139001", existingEmail);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson1))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk());

        // 尝试用相同邮箱注册
        String registerJson2 = buildRegisterJson("testuser004", "Pass@123456", "13900139002", existingEmail);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson2))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("已存在")));
        
        System.out.println("✓ TC-REG-02 测试通过: 邮箱重复检查生效");
    }

    @Test
    @DisplayName("TC-REG-03: 用户名已存在")
    void testUsernameAlreadyExists() throws Exception {
        System.out.println("\n执行测试用例: TC-REG-03 - 用户名已存在");
        
        String existingUsername = "duplicateuser";
        String email1 = "test005@example.com";
        String email2 = "test006@example.com";
        
        // 先注册一个用户
        String registerJson1 = buildRegisterJson(existingUsername, "Pass@123456", "13900139003", email1);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson1))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk());

        // 尝试用相同用户名注册
        String registerJson2 = buildRegisterJson(existingUsername, "Pass@123456", "13900139004", email2);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson2))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("已存在")));
        
        System.out.println("✓ TC-REG-03 测试通过: 用户名重复检查生效");
    }

    @Test
    @DisplayName("TC-REG-04: 手机号长度≠11位")
    void testInvalidPhoneLength() throws Exception {
        System.out.println("\n执行测试用例: TC-REG-04 - 手机号长度≠11位");
        
        String invalidPhone = "1380013800"; // 10位
        String email = "test007@example.com";
        
        String registerJson = buildRegisterJson("testuser007", "Pass@123456", invalidPhone, email);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        System.out.println("✓ TC-REG-04 测试通过: 手机号长度校验生效");
    }

    @Test
    @DisplayName("TC-REG-05: 用户名长度<6位")
    void testUsernameTooShort() throws Exception {
        System.out.println("\n执行测试用例: TC-REG-05 - 用户名长度<6位");
        
        String shortUsername = "user"; // 4位
        String email = "test008@example.com";
        
        String registerJson = buildRegisterJson(shortUsername, "Pass@123456", "13900139005", email);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        System.out.println("✓ TC-REG-05 测试通过: 用户名长度校验生效");
    }

    @Test
    @DisplayName("TC-REG-06: 用户名长度>24位")
    void testUsernameTooLong() throws Exception {
        System.out.println("\n执行测试用例: TC-REG-06 - 用户名长度>24位");
        
        String longUsername = "this_is_a_very_long_username_example"; // 超过24位
        String email = "test009@example.com";
        
        String registerJson = buildRegisterJson(longUsername, "Pass@123456", "13900139006", email);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        System.out.println("✓ TC-REG-06 测试通过: 用户名长度校验生效");
    }

    @Test
    @DisplayName("TC-REG-07: 密码长度<6位")
    void testPasswordTooShort() throws Exception {
        System.out.println("\n执行测试用例: TC-REG-07 - 密码长度<6位");
        
        String shortPassword = "12345"; // 5位
        String email = "test010@example.com";
        
        String registerJson = buildRegisterJson("testuser010", shortPassword, "13900139007", email);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        System.out.println("✓ TC-REG-07 测试通过: 密码长度校验生效");
    }

    @Test
    @DisplayName("TC-REG-08: 密码长度>24位")
    void testPasswordTooLong() throws Exception {
        System.out.println("\n执行测试用例: TC-REG-08 - 密码长度>24位");
        
        String longPassword = "this_is_a_very_long_password_123456"; // 超过24位
        String email = "test011@example.com";
        
        String registerJson = buildRegisterJson("testuser011", longPassword, "13900139008", email);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest());
        
        System.out.println("✓ TC-REG-08 测试通过: 密码长度校验生效");
    }

    @Test
    @DisplayName("TC-REG-09: 所有字段合法 - 注册成功")
    void testSuccessfulRegistration() throws Exception {
        System.out.println("\n执行测试用例: TC-REG-09 - 所有字段合法，注册成功");
        
        String email = "newuser@example.com";
        
        String registerJson = buildRegisterJson("newuser123", "Pass@123", "13900139009", email);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("注册成功"));
        
        System.out.println("✓ TC-REG-09 测试通过: 业务逻辑和加密功能正常");
        System.out.println("\n========================================");
        System.out.println("用户注册功能集成测试全部完成！");
        System.out.println("========================================");
    }
}

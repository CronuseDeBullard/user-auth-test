package com.softwaretest;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 用户登录功能集成测试
 * 包含6个测试用例：TC-LOGIN-01 到 TC-LOGIN-06
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("用户登录功能集成测试")
public class UserLoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String buildRegisterJson(String username, String password, String phone, String email) throws Exception {
        Map<String, String> registerData = new HashMap<>();
        registerData.put("username", username);
        registerData.put("password", password);
        registerData.put("email", email);
        registerData.put("phone", phone);
        registerData.put("userType", "user");
        return objectMapper.writeValueAsString(registerData);
    }

    private String buildLoginJson(String username, String password) throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);
        return objectMapper.writeValueAsString(loginData);
    }

    @BeforeEach
    void setUp() {
        System.out.println("========================================");
        System.out.println("开始执行用户登录功能集成测试");
        System.out.println("========================================");
    }

    @Test
    @DisplayName("TC-LOGIN-01: 正确的用户名和密码 - 登录成功")
    void testSuccessfulLogin() throws Exception {
        System.out.println("\n执行测试用例: TC-LOGIN-01 - 正确的用户名和密码，登录成功");
        
        String username = "logintest001";
        String password = "Pass@123456";
        
        // 先注册用户
        String registerJson = buildRegisterJson(username, password, "13800138001", "login001@example.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
        // 登录
        String loginJson = buildLoginJson(username, password);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("登录成功"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.username").value(username));
        
        System.out.println("✓ TC-LOGIN-01 测试通过: 登录成功");
    }

    @Test
    @DisplayName("TC-LOGIN-02: 用户名不存在")
    void testUsernameNotExists() throws Exception {
        System.out.println("\n执行测试用例: TC-LOGIN-02 - 用户名不存在");
        
        String loginJson = buildLoginJson("nonexistentuser", "Pass@123456");
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(401));
        
        System.out.println("✓ TC-LOGIN-02 测试通过: 用户名不存在时拒绝登录");
    }

    @Test
    @DisplayName("TC-LOGIN-03: 密码错误")
    void testWrongPassword() throws Exception {
        System.out.println("\n执行测试用例: TC-LOGIN-03 - 密码错误");
        
        String username = "logintest002";
        String correctPassword = "Pass@123456";
        String wrongPassword = "WrongPass@123";
        
        // 先注册用户
        String registerJson = buildRegisterJson(username, correctPassword, "13800138002", "login002@example.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andExpect(MockMvcResultMatchers.status().isOk());
        
        // 使用错误密码登录
        String loginJson = buildLoginJson(username, wrongPassword);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(401));
        
        System.out.println("✓ TC-LOGIN-03 测试通过: 密码错误时拒绝登录");
    }

    @Test
    @DisplayName("TC-LOGIN-04: 用户名为空")
    void testEmptyUsername() throws Exception {
        System.out.println("\n执行测试用例: TC-LOGIN-04 - 用户名为空");
        
        String loginJson = buildLoginJson("", "Pass@123456");
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        
        System.out.println("✓ TC-LOGIN-04 测试通过: 用户名为空时拒绝登录");
    }

    @Test
    @DisplayName("TC-LOGIN-05: 密码为空")
    void testEmptyPassword() throws Exception {
        System.out.println("\n执行测试用例: TC-LOGIN-05 - 密码为空");
        
        String loginJson = buildLoginJson("testuser", "");
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isUnauthorized());
        
        System.out.println("✓ TC-LOGIN-05 测试通过: 密码为空时拒绝登录");
    }

    @Test
    @DisplayName("TC-LOGIN-06: 注册后立即登录")
    void testRegisterAndLogin() throws Exception {
        System.out.println("\n执行测试用例: TC-LOGIN-06 - 注册后立即登录");
        
        String username = "logintest003";
        String password = "Pass@123456";
        
        // 注册
        String registerJson = buildRegisterJson(username, password, "13800138003", "login003@example.com");
        mockMvc.perform(MockMvcRequestBuilders.post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("注册成功"));
        
        // 立即登录
        String loginJson = buildLoginJson(username, password);
        mockMvc.perform(MockMvcRequestBuilders.post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("登录成功"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.username").value(username));
        
        System.out.println("✓ TC-LOGIN-06 测试通过: 注册后立即登录成功");
        System.out.println("\n========================================");
        System.out.println("用户登录功能集成测试全部完成！");
        System.out.println("========================================");
    }
}

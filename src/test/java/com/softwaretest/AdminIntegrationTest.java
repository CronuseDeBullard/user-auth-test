package com.softwaretest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaretest.entity.User;
import com.softwaretest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员端功能集成测试
 * 包含6个测试用例：TC-ADMIN-01 到 TC-ADMIN-06
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("管理员端功能集成测试")
public class AdminIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        System.out.println("========================================");
        System.out.println("开始执行管理员端功能集成测试");
        System.out.println("========================================");
    }

    @Test
    @DisplayName("TC-ADMIN-01: 查询所有用户 - 成功查询")
    void testGetAllUsersSuccess() throws Exception {
        System.out.println("\n执行测试用例: TC-ADMIN-01 - 查询所有用户成功");
        
        // 创建测试用户
        User user1 = new User();
        user1.setUsername("teacher001");
        user1.setPassword(passwordEncoder.encode("password"));
        user1.setEmail("teacher001@test.com");
        user1.setPhone("13800000001");
        user1.setUserType("teacher");
        userRepository.save(user1);
        
        User user2 = new User();
        user2.setUsername("student001");
        user2.setPassword(passwordEncoder.encode("password"));
        user2.setEmail("student001@test.com");
        user2.setPhone("13800000002");
        user2.setUserType("student");
        userRepository.save(user2);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.total").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.users").isArray());
        
        System.out.println("✓ TC-ADMIN-01 测试通过: 查询所有用户成功");
    }

    @Test
    @DisplayName("TC-ADMIN-02: 按用户类型查询 - 查询教师")
    void testGetUsersByTypeTeacher() throws Exception {
        System.out.println("\n执行测试用例: TC-ADMIN-02 - 按类型查询教师");
        
        // 创建不同类型用户
        User teacher = new User();
        teacher.setUsername("teacher002");
        teacher.setPassword(passwordEncoder.encode("password"));
        teacher.setEmail("teacher002@test.com");
        teacher.setPhone("13800000003");
        teacher.setUserType("teacher");
        userRepository.save(teacher);
        
        User student = new User();
        student.setUsername("student002");
        student.setPassword(passwordEncoder.encode("password"));
        student.setEmail("student002@test.com");
        student.setPhone("13800000004");
        student.setUserType("student");
        userRepository.save(student);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users")
                .param("userType", "teacher"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.total").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.users[0].userType").value("teacher"));
        
        System.out.println("✓ TC-ADMIN-02 测试通过: 按类型查询教师成功");
    }

    @Test
    @DisplayName("TC-ADMIN-03: 按用户类型查询 - 查询学生")
    void testGetUsersByTypeStudent() throws Exception {
        System.out.println("\n执行测试用例: TC-ADMIN-03 - 按类型查询学生");
        
        User student1 = new User();
        student1.setUsername("student003");
        student1.setPassword(passwordEncoder.encode("password"));
        student1.setEmail("student003@test.com");
        student1.setPhone("13800000005");
        student1.setUserType("student");
        userRepository.save(student1);
        
        User student2 = new User();
        student2.setUsername("student004");
        student2.setPassword(passwordEncoder.encode("password"));
        student2.setEmail("student004@test.com");
        student2.setPhone("13800000006");
        student2.setUserType("student");
        userRepository.save(student2);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/users")
                .param("userType", "student"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.total").value(2));
        
        System.out.println("✓ TC-ADMIN-03 测试通过: 按类型查询学生成功");
    }

    @Test
    @DisplayName("TC-ADMIN-04: 删除用户 - 成功删除")
    void testDeleteUserSuccess() throws Exception {
        System.out.println("\n执行测试用例: TC-ADMIN-04 - 删除用户成功");
        
        User user = new User();
        user.setUsername("todelete");
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("todelete@test.com");
        user.setPhone("13800000007");
        user.setUserType("student");
        User savedUser = userRepository.save(user);
        
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/" + savedUser.getId()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("删除成功"));
        
        // 验证用户已被删除
        assert !userRepository.existsById(savedUser.getId());
        
        System.out.println("✓ TC-ADMIN-04 测试通过: 删除用户成功");
    }

    @Test
    @DisplayName("TC-ADMIN-05: 删除用户 - 用户不存在")
    void testDeleteUserNotFound() throws Exception {
        System.out.println("\n执行测试用例: TC-ADMIN-05 - 删除不存在的用户");
        
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/users/9999"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("不存在")));
        
        System.out.println("✓ TC-ADMIN-05 测试通过: 用户不存在时拒绝删除");
    }

    @Test
    @DisplayName("TC-ADMIN-06: 更新用户状态 - 成功更新")
    void testUpdateUserStatusSuccess() throws Exception {
        System.out.println("\n执行测试用例: TC-ADMIN-06 - 更新用户状态成功");
        
        User user = new User();
        user.setUsername("statustest");
        user.setPassword(passwordEncoder.encode("password"));
        user.setEmail("statustest@test.com");
        user.setPhone("13800000008");
        user.setUserType("student");
        User savedUser = userRepository.save(user);
        
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin/users/" + savedUser.getId() + "/status")
                .param("status", "active"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("更新成功"));
        
        System.out.println("✓ TC-ADMIN-06 测试通过: 更新用户状态成功");
        System.out.println("\n========================================");
        System.out.println("管理员端功能集成测试全部完成！");
        System.out.println("========================================");
    }
}

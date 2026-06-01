package com.softwaretest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaretest.entity.Exam;
import com.softwaretest.repository.ExamRepository;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 教师端功能集成测试
 * 包含6个测试用例：TC-TEACHER-01 到 TC-TEACHER-06
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("教师端功能集成测试")
public class TeacherIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        examRepository.deleteAll();
        userRepository.deleteAll();
        System.out.println("========================================");
        System.out.println("开始执行教师端功能集成测试");
        System.out.println("========================================");
    }

    @Test
    @DisplayName("TC-TEACHER-01: 创建考试 - 所有字段合法")
    void testCreateExamSuccess() throws Exception {
        System.out.println("\n执行测试用例: TC-TEACHER-01 - 创建考试成功");
        
        Map<String, Object> examData = new HashMap<>();
        examData.put("title", "Java程序设计期末考试");
        examData.put("subject", "Java");
        examData.put("duration", 120);
        examData.put("totalScore", 100);
        examData.put("startTime", LocalDateTime.now().plusDays(1).toString());
        examData.put("endTime", LocalDateTime.now().plusDays(1).plusHours(2).toString());
        examData.put("status", 0);
        examData.put("teacherId", 1L);
        
        String json = objectMapper.writeValueAsString(examData);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/teacher/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("创建成功"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.examId").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("Java程序设计期末考试"));
        
        System.out.println("✓ TC-TEACHER-01 测试通过: 考试创建成功");
    }

    @Test
    @DisplayName("TC-TEACHER-02: 创建考试 - 标题为空")
    void testCreateExamEmptyTitle() throws Exception {
        System.out.println("\n执行测试用例: TC-TEACHER-02 - 标题为空");
        
        Map<String, Object> examData = new HashMap<>();
        examData.put("title", "");
        examData.put("subject", "Java");
        examData.put("duration", 120);
        examData.put("totalScore", 100);
        examData.put("teacherId", 1L);
        
        String json = objectMapper.writeValueAsString(examData);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/teacher/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(400));
        
        System.out.println("✓ TC-TEACHER-02 测试通过: 标题为空时拒绝创建");
    }

    @Test
    @DisplayName("TC-TEACHER-03: 创建考试 - 时长小于等于0")
    void testCreateExamInvalidDuration() throws Exception {
        System.out.println("\n执行测试用例: TC-TEACHER-03 - 时长小于等于0");
        
        Map<String, Object> examData = new HashMap<>();
        examData.put("title", "测试考试");
        examData.put("subject", "Java");
        examData.put("duration", 0);
        examData.put("totalScore", 100);
        examData.put("teacherId", 1L);
        
        String json = objectMapper.writeValueAsString(examData);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/teacher/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("时长")));
        
        System.out.println("✓ TC-TEACHER-03 测试通过: 时长校验生效");
    }

    @Test
    @DisplayName("TC-TEACHER-04: 创建考试 - 总分小于等于0")
    void testCreateExamInvalidTotalScore() throws Exception {
        System.out.println("\n执行测试用例: TC-TEACHER-04 - 总分小于等于0");
        
        Map<String, Object> examData = new HashMap<>();
        examData.put("title", "测试考试");
        examData.put("subject", "Java");
        examData.put("duration", 120);
        examData.put("totalScore", -10);
        examData.put("teacherId", 1L);
        
        String json = objectMapper.writeValueAsString(examData);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/teacher/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("总分")));
        
        System.out.println("✓ TC-TEACHER-04 测试通过: 总分校验生效");
    }

    @Test
    @DisplayName("TC-TEACHER-05: 查询考试列表 - 按教师ID")
    void testGetExamsByTeacher() throws Exception {
        System.out.println("\n执行测试用例: TC-TEACHER-05 - 按教师ID查询");
        
        // 先创建几个考试
        Exam exam1 = new Exam();
        exam1.setTitle("考试1");
        exam1.setSubject("Java");
        exam1.setDuration(120);
        exam1.setTotalScore(100);
        exam1.setTeacherId(1L);
        examRepository.save(exam1);
        
        Exam exam2 = new Exam();
        exam2.setTitle("考试2");
        exam2.setSubject("Python");
        exam2.setDuration(90);
        exam2.setTotalScore(100);
        exam2.setTeacherId(1L);
        examRepository.save(exam2);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/teacher/exams")
                .param("teacherId", "1"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.total").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.exams").isArray());
        
        System.out.println("✓ TC-TEACHER-05 测试通过: 按教师ID查询成功");
    }

    @Test
    @DisplayName("TC-TEACHER-06: 查询考试列表 - 按科目")
    void testGetExamsBySubject() throws Exception {
        System.out.println("\n执行测试用例: TC-TEACHER-06 - 按科目查询");
        
        // 创建考试
        Exam exam = new Exam();
        exam.setTitle("数据结构考试");
        exam.setSubject("数据结构");
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam.setTeacherId(1L);
        examRepository.save(exam);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/teacher/exams")
                .param("subject", "数据结构"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.total").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.exams[0].subject").value("数据结构"));
        
        System.out.println("✓ TC-TEACHER-06 测试通过: 按科目查询成功");
        System.out.println("\n========================================");
        System.out.println("教师端功能集成测试全部完成！");
        System.out.println("========================================");
    }
}

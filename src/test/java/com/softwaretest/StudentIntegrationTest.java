package com.softwaretest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaretest.entity.Exam;
import com.softwaretest.entity.ExamRecord;
import com.softwaretest.repository.ExamRecordRepository;
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

/**
 * 学生端功能集成测试
 * 包含6个测试用例：TC-STUDENT-01 到 TC-STUDENT-06
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("学生端功能集成测试")
public class StudentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private ExamRecordRepository examRecordRepository;
    
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        examRecordRepository.deleteAll();
        examRepository.deleteAll();
        userRepository.deleteAll();
        System.out.println("========================================");
        System.out.println("开始执行学生端功能集成测试");
        System.out.println("========================================");
    }

    @Test
    @DisplayName("TC-STUDENT-01: 提交考试 - 成功提交")
    void testSubmitExamSuccess() throws Exception {
        System.out.println("\n执行测试用例: TC-STUDENT-01 - 提交考试成功");
        
        // 先创建一个考试
        Exam exam = new Exam();
        exam.setTitle("Java期末考试");
        exam.setSubject("Java");
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam.setTeacherId(1L);
        exam.setStatus(1); // 进行中
        Exam savedExam = examRepository.save(exam);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/student/exams/" + savedExam.getId() + "/submit")
                .param("studentId", "1")
                .param("score", "85.5"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("提交成功"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.score").value(85.5))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value(1));
        
        System.out.println("✓ TC-STUDENT-01 测试通过: 考试提交成功");
    }

    @Test
    @DisplayName("TC-STUDENT-02: 提交考试 - 考试不存在")
    void testSubmitExamNotFound() throws Exception {
        System.out.println("\n执行测试用例: TC-STUDENT-02 - 考试不存在");
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/student/exams/9999/submit")
                .param("studentId", "1")
                .param("score", "85.5"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("不存在")));
        
        System.out.println("✓ TC-STUDENT-02 测试通过: 考试不存在时拒绝提交");
    }

    @Test
    @DisplayName("TC-STUDENT-03: 查询考试记录 - 成功查询")
    void testGetRecordsSuccess() throws Exception {
        System.out.println("\n执行测试用例: TC-STUDENT-03 - 查询考试记录成功");
        
        // 创建考试
        Exam exam = new Exam();
        exam.setTitle("测试考试");
        exam.setSubject("Java");
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam.setTeacherId(1L);
        Exam savedExam = examRepository.save(exam);
        
        // 创建考试记录
        ExamRecord record = new ExamRecord();
        record.setExamId(savedExam.getId());
        record.setStudentId(1L);
        record.setScore(90.0);
        record.setStatus(1);
        examRecordRepository.save(record);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/student/records")
                .param("studentId", "1"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.total").value(1))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.records[0].score").value(90.0));
        
        System.out.println("✓ TC-STUDENT-03 测试通过: 查询考试记录成功");
    }

    @Test
    @DisplayName("TC-STUDENT-04: 查询考试记录 - 无记录")
    void testGetRecordsEmpty() throws Exception {
        System.out.println("\n执行测试用例: TC-STUDENT-04 - 无考试记录");
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/student/records")
                .param("studentId", "999"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.total").value(0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.records").isEmpty());
        
        System.out.println("✓ TC-STUDENT-04 测试通过: 无记录时返回空列表");
    }

    @Test
    @DisplayName("TC-STUDENT-05: 查询考试结果 - 成功查询")
    void testGetResultSuccess() throws Exception {
        System.out.println("\n执行测试用例: TC-STUDENT-05 - 查询考试结果成功");
        
        // 创建考试
        Exam exam = new Exam();
        exam.setTitle("期末考试");
        exam.setSubject("Java");
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam.setTeacherId(1L);
        Exam savedExam = examRepository.save(exam);
        
        // 创建考试记录
        ExamRecord record = new ExamRecord();
        record.setExamId(savedExam.getId());
        record.setStudentId(1L);
        record.setScore(88.0);
        record.setStatus(2); // 已批改
        examRecordRepository.save(record);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/student/exams/" + savedExam.getId() + "/result")
                .param("studentId", "1"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.score").value(88.0))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.status").value(2));
        
        System.out.println("✓ TC-STUDENT-05 测试通过: 查询考试结果成功");
    }

    @Test
    @DisplayName("TC-STUDENT-06: 查询考试结果 - 记录不存在")
    void testGetResultNotFound() throws Exception {
        System.out.println("\n执行测试用例: TC-STUDENT-06 - 考试记录不存在");
        
        // 创建考试但不创建记录
        Exam exam = new Exam();
        exam.setTitle("测试考试");
        exam.setSubject("Java");
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam.setTeacherId(1L);
        Exam savedExam = examRepository.save(exam);
        
        mockMvc.perform(MockMvcRequestBuilders.get("/api/student/exams/" + savedExam.getId() + "/result")
                .param("studentId", "999"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.status().isBadRequest())
            .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(org.hamcrest.Matchers.containsString("未找到")));
        
        System.out.println("✓ TC-STUDENT-06 测试通过: 记录不存在时返回错误");
        System.out.println("\n========================================");
        System.out.println("学生端功能集成测试全部完成！");
        System.out.println("========================================");
    }
}

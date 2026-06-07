package com.softwaretest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaretest.entity.Exam;
import com.softwaretest.entity.ExamRecord;
import com.softwaretest.repository.ExamRecordRepository;
import com.softwaretest.repository.ExamRepository;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 学生端考试集成测试
 * 基于 StudentExamServiceUnitTest 的单元测试
 * 测试完整的获取试卷、提交答案、查询成绩流程
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("学生端考试集成测试")
public class StudentExamIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private ExamRecordRepository examRecordRepository;

    @BeforeEach
    void setUp() {
        examRecordRepository.deleteAll();
        examRepository.deleteAll();
    }

    @Test
    @DisplayName("IT-STUDENT-01: 完整的考试流程")
    void testCompleteExamFlow() throws Exception {
        // 1. 创建一个进行中的考试
        Exam exam = createOngoingExam("期末考试", "软件测试");

        // 2. 学生获取试卷
        mockMvc.perform(get("/api/student/exams/" + exam.getId() + "/paper"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("期末考试"))
                .andExpect(jsonPath("$.data.questions").isArray());

        // 3. 学生提交答案
        Map<String, Object> answers = new HashMap<>();
        answers.put("1", "A");
        answers.put("2", "B");
        
        String submitJson = objectMapper.writeValueAsString(answers);
        mockMvc.perform(post("/api/student/exams/" + exam.getId() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. 查询考试记录
        mockMvc.perform(get("/api/student/exam-records")
                        .param("examId", exam.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("IT-STUDENT-02: 未开始的考试不能获取试卷")
    void testCannotGetPaperForUpcomingExam() throws Exception {
        // 创建未开始的考试
        Exam exam = createUpcomingExam("未来考试", "软件测试");

        // 尝试获取试卷应该失败
        mockMvc.perform(get("/api/student/exams/" + exam.getId() + "/paper"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("IT-STUDENT-03: 已结束的考试不能提交答案")
    void testCannotSubmitForFinishedExam() throws Exception {
        // 创建已结束的考试
        Exam exam = createFinishedExam("过去考试", "软件测试");

        // 尝试提交答案应该失败
        Map<String, Object> answers = new HashMap<>();
        answers.put("1", "A");
        
        String submitJson = objectMapper.writeValueAsString(answers);
        mockMvc.perform(post("/api/student/exams/" + exam.getId() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitJson))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("answerValidationCases")
    @DisplayName("IT-STUDENT-04: 答案格式验证")
    void testAnswerValidation(String caseName, Map<String, Object> answers, int expectedStatus) throws Exception {
        // 创建进行中的考试
        Exam exam = createOngoingExam("测试考试", "软件测试");

        String submitJson = objectMapper.writeValueAsString(answers);
        mockMvc.perform(post("/api/student/exams/" + exam.getId() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitJson))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    @DisplayName("IT-STUDENT-05: 重复提交答案")
    void testResubmitAnswers() throws Exception {
        // 创建进行中的考试
        Exam exam = createOngoingExam("可重复提交考试", "软件测试");

        // 第一次提交
        Map<String, Object> answers1 = new HashMap<>();
        answers1.put("1", "A");
        
        String submitJson1 = objectMapper.writeValueAsString(answers1);
        mockMvc.perform(post("/api/student/exams/" + exam.getId() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitJson1))
                .andExpect(status().isOk());

        // 第二次提交（修改答案）
        Map<String, Object> answers2 = new HashMap<>();
        answers2.put("1", "B");
        
        String submitJson2 = objectMapper.writeValueAsString(answers2);
        mockMvc.perform(post("/api/student/exams/" + exam.getId() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(submitJson2))
                .andExpect(status().isOk());

        // 验证只有一条记录
        long recordCount = examRecordRepository.count();
        assertTrue(recordCount <= 1, "应该只有一条考试记录");
    }

    @Test
    @DisplayName("IT-STUDENT-06: 查询所有考试记录")
    void testGetAllExamRecords() throws Exception {
        // 创建多个考试并提交
        Exam exam1 = createFinishedExam("考试1", "软件测试");
        Exam exam2 = createFinishedExam("考试2", "数据库");

        // 创建考试记录
        createExamRecord(exam1.getId(), 1L, 85);
        createExamRecord(exam2.getId(), 1L, 90);

        // 查询所有记录
        mockMvc.perform(get("/api/student/exam-records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("IT-STUDENT-07: 按科目查询考试")
    void testGetExamsBySubject() throws Exception {
        // 创建不同科目的考试
        createOngoingExam("软件测试考试", "软件测试");
        createOngoingExam("数据库考试", "数据库");

        // 查询软件测试科目的考试
        mockMvc.perform(get("/api/student/exams")
                        .param("subject", "软件测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    // 数据提供方法
    static Stream<Arguments> answerValidationCases() {
        Map<String, Object> validAnswers = new HashMap<>();
        validAnswers.put("1", "A");
        validAnswers.put("2", "B");

        Map<String, Object> emptyAnswers = new HashMap<>();

        return Stream.of(
                Arguments.of("正常答案", validAnswers, 200),
                Arguments.of("空答案", emptyAnswers, 200)
        );
    }

    // 辅助方法
    private Exam createOngoingExam(String title, String subject) {
        Exam exam = new Exam();
        exam.setTitle(title);
        exam.setSubject(subject);
        exam.setStartTime(LocalDateTime.now().minusMinutes(10));
        exam.setEndTime(LocalDateTime.now().plusMinutes(50));
        exam.setDuration(60);
        exam.setTotalScore(100);
        exam.setStatus(1); // 进行中
        return examRepository.save(exam);
    }

    private Exam createUpcomingExam(String title, String subject) {
        Exam exam = new Exam();
        exam.setTitle(title);
        exam.setSubject(subject);
        exam.setStartTime(LocalDateTime.now().plusDays(1));
        exam.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam.setStatus(0); // 未开始
        return examRepository.save(exam);
    }

    private Exam createFinishedExam(String title, String subject) {
        Exam exam = new Exam();
        exam.setTitle(title);
        exam.setSubject(subject);
        exam.setStartTime(LocalDateTime.now().minusHours(3));
        exam.setEndTime(LocalDateTime.now().minusHours(1));
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam.setStatus(2); // 已结束
        return examRepository.save(exam);
    }

    private void createExamRecord(Long examId, Long studentId, Integer score) {
        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setStudentId(studentId);
        record.setScore(score.doubleValue());
        record.setSubmitTime(LocalDateTime.now());
        examRecordRepository.save(record);
    }
}

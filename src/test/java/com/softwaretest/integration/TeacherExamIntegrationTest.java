package com.softwaretest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softwaretest.entity.Exam;
import com.softwaretest.repository.ExamRepository;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 教师端考试管理集成测试
 * 基于 ExamServiceUnitTest 和 QuestionServiceUnitTest 的单元测试
 * 测试完整的题库管理、试卷创建、考试发布流程
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("教师端考试管理集成测试")
public class TeacherExamIntegrationTest {

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
    }

    @Test
    @DisplayName("IT-TEACHER-01: 完整的创建考试流程")
    void testCompleteExamCreationFlow() throws Exception {
        String examName = "期末考试";
        String subject = "软件测试";
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        Integer duration = 120;
        Integer totalScore = 100;

        // 1. 创建考试
        String createExamJson = buildCreateExamJson(examName, subject, startTime, duration, totalScore);
        String response = mockMvc.perform(post("/api/teacher/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createExamJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn().getResponse().getContentAsString();

        // 2. 验证考试已创建
        assertTrue(examRepository.count() > 0, "考试应该已创建");

        // 3. 查询考试列表
        mockMvc.perform(get("/api/teacher/exams")
                        .param("subject", subject))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value(examName));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("examCreationValidationCases")
    @DisplayName("IT-TEACHER-02: 考试创建参数验证")
    void testExamCreationValidation(String caseName, String examName, String subject,
                                    LocalDateTime startTime, Integer duration,
                                    Integer totalScore, int expectedStatus) throws Exception {
        String createExamJson = buildCreateExamJson(examName, subject, startTime, duration, totalScore);

        mockMvc.perform(post("/api/teacher/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createExamJson))
                .andExpect(status().is(expectedStatus));
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1}")
    @MethodSource("examStatusCalculationCases")
    @DisplayName("IT-TEACHER-03: 考试状态计算测试")
    void testExamStatusCalculation(String caseName, LocalDateTime startTime,
                                   LocalDateTime endTime, String expectedStatus) throws Exception {
        // 创建考试
        Exam exam = new Exam();
        exam.setTitle("测试考试");
        exam.setSubject("软件测试");
        exam.setStartTime(startTime);
        exam.setEndTime(endTime);
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam = examRepository.save(exam);

        // 查询考试详情，验证状态
        mockMvc.perform(get("/api/teacher/exams/" + exam.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(expectedStatus));
    }

    @Test
    @DisplayName("IT-TEACHER-04: 考试时长和及格分数默认值测试")
    void testExamDefaultValues() throws Exception {
        String examName = "默认值测试考试";
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);

        // 不指定时长和及格分数
        Map<String, Object> examData = new HashMap<>();
        examData.put("title", examName);
        examData.put("subject", "软件测试");
        examData.put("startTime", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        examData.put("totalScore", 100);

        String createExamJson = objectMapper.writeValueAsString(examData);
        mockMvc.perform(post("/api/teacher/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createExamJson))
                .andExpect(status().isOk());

        // 验证默认值
        Exam exam = examRepository.findAll().get(0);
        assertEquals(60, exam.getDuration(), "默认时长应为60分钟");
    }

    @Test
    @DisplayName("IT-TEACHER-05: 考试结束时间自动计算")
    void testExamEndTimeCalculation() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        Integer duration = 90; // 90分钟

        String createExamJson = buildCreateExamJson("时间计算测试", "软件测试", startTime, duration, 100);
        mockMvc.perform(post("/api/teacher/exams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createExamJson))
                .andExpect(status().isOk());

        // 验证结束时间 = 开始时间 + 时长
        Exam exam = examRepository.findAll().get(0);
        LocalDateTime expectedEndTime = startTime.plusMinutes(duration);
        assertEquals(expectedEndTime, exam.getEndTime(), "结束时间应该等于开始时间加上时长");
    }

    @Test
    @DisplayName("IT-TEACHER-06: 更新考试信息")
    void testUpdateExam() throws Exception {
        // 先创建考试
        Exam exam = new Exam();
        exam.setTitle("原始考试");
        exam.setSubject("软件测试");
        exam.setStartTime(LocalDateTime.now().plusDays(1));
        exam.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam = examRepository.save(exam);

        // 更新考试
        String newTitle = "更新后的考试";
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("title", newTitle);
        updateData.put("totalScore", 150);

        String updateJson = objectMapper.writeValueAsString(updateData);
        mockMvc.perform(put("/api/teacher/exams/" + exam.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk());

        // 验证更新
        Exam updated = examRepository.findById(exam.getId()).orElseThrow();
        assertEquals(newTitle, updated.getTitle());
        assertEquals(150, updated.getTotalScore());
    }

    @Test
    @DisplayName("IT-TEACHER-07: 删除考试")
    void testDeleteExam() throws Exception {
        // 创建考试
        Exam exam = new Exam();
        exam.setTitle("待删除考试");
        exam.setSubject("软件测试");
        exam.setStartTime(LocalDateTime.now().plusDays(1));
        exam.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam = examRepository.save(exam);

        Long examId = exam.getId();

        // 删除考试
        mockMvc.perform(delete("/api/teacher/exams/" + examId))
                .andExpect(status().isOk());

        // 验证已删除
        assertFalse(examRepository.existsById(examId), "考试应该已被删除");
    }

    @Test
    @DisplayName("IT-TEACHER-08: 按科目查询考试")
    void testQueryExamsBySubject() throws Exception {
        // 创建不同科目的考试
        createExam("数学考试", "数学");
        createExam("英语考试", "英语");
        createExam("软件测试考试", "软件测试");

        // 查询软件测试科目的考试
        mockMvc.perform(get("/api/teacher/exams")
                        .param("subject", "软件测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].subject").value("软件测试"));
    }

    // 数据提供方法
    static Stream<Arguments> examCreationValidationCases() {
        LocalDateTime validStartTime = LocalDateTime.now().plusDays(1);
        return Stream.of(
                Arguments.of("考试名称为空", "", "软件测试", validStartTime, 120, 100, 400),
                Arguments.of("考试名称为null", null, "软件测试", validStartTime, 120, 100, 400),
                Arguments.of("科目为空", "期末考试", "", validStartTime, 120, 100, 400),
                Arguments.of("开始时间为null", "期末考试", "软件测试", null, 120, 100, 400),
                Arguments.of("时长为0", "期末考试", "软件测试", validStartTime, 0, 100, 400),
                Arguments.of("时长为负数", "期末考试", "软件测试", validStartTime, -10, 100, 400),
                Arguments.of("总分为0", "期末考试", "软件测试", validStartTime, 120, 0, 400),
                Arguments.of("总分为负数", "期末考试", "软件测试", validStartTime, 120, -100, 400),
                Arguments.of("正常创建", "期末考试", "软件测试", validStartTime, 120, 100, 200)
        );
    }

    static Stream<Arguments> examStatusCalculationCases() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                Arguments.of("未开始的考试", now.plusDays(1), now.plusDays(1).plusHours(2), "upcoming"),
                Arguments.of("进行中的考试", now.minusMinutes(10), now.plusMinutes(50), "ongoing"),
                Arguments.of("已结束的考试", now.minusHours(3), now.minusHours(1), "finished")
        );
    }

    // 辅助方法
    private String buildCreateExamJson(String title, String subject, LocalDateTime startTime,
                                      Integer duration, Integer totalScore) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("subject", subject);
        if (startTime != null) {
            data.put("startTime", startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        data.put("duration", duration);
        data.put("totalScore", totalScore);
        return objectMapper.writeValueAsString(data);
    }

    private void createExam(String title, String subject) {
        Exam exam = new Exam();
        exam.setTitle(title);
        exam.setSubject(subject);
        exam.setStartTime(LocalDateTime.now().plusDays(1));
        exam.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        exam.setDuration(120);
        exam.setTotalScore(100);
        examRepository.save(exam);
    }
}

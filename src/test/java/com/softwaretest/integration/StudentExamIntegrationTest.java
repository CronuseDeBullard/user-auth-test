package com.softwaretest.integration;

import com.softwaretest.entity.Exam;
import com.softwaretest.entity.ExamRecord;
import com.softwaretest.repository.ExamRecordRepository;
import com.softwaretest.repository.ExamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 学生端考试集成测试
 * 测试学生提交考试、查询记录、查询成绩功能
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
    private ExamRepository examRepository;

    @Autowired
    private ExamRecordRepository examRecordRepository;

    @BeforeEach
    void setUp() {
        examRecordRepository.deleteAll();
        examRepository.deleteAll();
    }

    @Test
    @DisplayName("IT-STUDENT-01: 提交考试成绩")
    void testSubmitExam() throws Exception {
        // 创建考试
        Exam exam = createExam("期末考试", "软件测试");
        Long studentId = 1L;
        Double score = 85.5;

        // 提交考试
        mockMvc.perform(post("/api/student/exams/" + exam.getId() + "/submit")
                        .param("studentId", studentId.toString())
                        .param("score", score.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("提交成功"))
                .andExpect(jsonPath("$.data.score").value(score));

        // 验证记录已创建
        assertTrue(examRecordRepository.count() > 0);
    }

    @Test
    @DisplayName("IT-STUDENT-02: 查询学生考试记录")
    void testGetStudentRecords() throws Exception {
        // 创建考试和记录
        Exam exam1 = createExam("考试1", "软件测试");
        Exam exam2 = createExam("考试2", "数据库");
        Long studentId = 1L;

        createExamRecord(exam1.getId(), studentId, 85.0);
        createExamRecord(exam2.getId(), studentId, 90.0);

        // 查询记录
        mockMvc.perform(get("/api/student/records")
                        .param("studentId", studentId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.total").value(2));
    }

    @Test
    @DisplayName("IT-STUDENT-03: 查询考试成绩")
    void testGetExamResult() throws Exception {
        // 创建考试和记录
        Exam exam = createExam("期末考试", "软件测试");
        Long studentId = 1L;
        Double score = 88.0;
        
        createExamRecord(exam.getId(), studentId, score);

        // 查询成绩
        mockMvc.perform(get("/api/student/exams/" + exam.getId() + "/result")
                        .param("studentId", studentId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.score").value(score));
    }

    @Test
    @DisplayName("IT-STUDENT-04: 查询不存在的考试记录")
    void testGetNonExistentRecord() throws Exception {
        Long examId = 999L;
        Long studentId = 1L;

        mockMvc.perform(get("/api/student/exams/" + examId + "/result")
                        .param("studentId", studentId.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // 辅助方法
    private Exam createExam(String title, String subject) {
        Exam exam = new Exam();
        exam.setTitle(title);
        exam.setSubject(subject);
        exam.setStartTime(LocalDateTime.now().minusHours(1));
        exam.setEndTime(LocalDateTime.now().plusHours(1));
        exam.setDuration(120);
        exam.setTotalScore(100);
        exam.setStatus(1); // 进行中
        return examRepository.save(exam);
    }

    private void createExamRecord(Long examId, Long studentId, Double score) {
        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setStudentId(studentId);
        record.setScore(score);
        record.setStatus(1); // 已提交
        record.setSubmitTime(LocalDateTime.now());
        examRecordRepository.save(record);
    }
}

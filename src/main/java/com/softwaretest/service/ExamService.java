package com.softwaretest.service;

import com.softwaretest.entity.Exam;
import com.softwaretest.entity.ExamRecord;
import com.softwaretest.repository.ExamRepository;
import com.softwaretest.repository.ExamRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {
    
    private final ExamRepository examRepository;
    private final ExamRecordRepository examRecordRepository;
    
    @Transactional
    public Exam createExam(Exam exam) {
        if (exam.getTitle() == null || exam.getTitle().isBlank()) {
            throw new RuntimeException("考试标题不能为空");
        }
        if (exam.getDuration() == null || exam.getDuration() <= 0) {
            throw new RuntimeException("考试时长必须大于0");
        }
        if (exam.getTotalScore() == null || exam.getTotalScore() <= 0) {
            throw new RuntimeException("总分必须大于0");
        }
        return examRepository.save(exam);
    }
    
    public List<Exam> getExamsByTeacher(Long teacherId) {
        return examRepository.findByTeacherId(teacherId);
    }
    
    public List<Exam> getExamsBySubject(String subject) {
        return examRepository.findBySubject(subject);
    }
    
    @Transactional
    public ExamRecord submitExam(Long examId, Long studentId, Double score) {
        Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new RuntimeException("考试不存在"));
        
        ExamRecord record = examRecordRepository.findByExamIdAndStudentId(examId, studentId)
            .orElse(new ExamRecord());
        
        record.setExamId(examId);
        record.setStudentId(studentId);
        record.setScore(score);
        record.setStatus(1); // 已提交
        record.setSubmitTime(LocalDateTime.now());
        
        return examRecordRepository.save(record);
    }
    
    public List<ExamRecord> getStudentRecords(Long studentId) {
        return examRecordRepository.findByStudentId(studentId);
    }
    
    public ExamRecord getExamRecord(Long examId, Long studentId) {
        return examRecordRepository.findByExamIdAndStudentId(examId, studentId)
            .orElse(null);
    }
}

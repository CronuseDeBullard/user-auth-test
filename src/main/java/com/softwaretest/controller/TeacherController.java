package com.softwaretest.controller;

import com.softwaretest.dto.ApiResponse;
import com.softwaretest.entity.Exam;
import com.softwaretest.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {
    
    private final ExamService examService;
    
    @PostMapping("/exams")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createExam(@Valid @RequestBody Exam exam) {
        try {
            Exam created = examService.createExam(exam);
            Map<String, Object> data = new HashMap<>();
            data.put("examId", created.getId());
            data.put("title", created.getTitle());
            return ResponseEntity.ok(ApiResponse.success("创建成功", data));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    @GetMapping("/exams")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExams(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String subject) {
        
        List<Exam> exams;
        if (teacherId != null) {
            exams = examService.getExamsByTeacher(teacherId);
        } else if (subject != null) {
            exams = examService.getExamsBySubject(subject);
        } else {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "请提供teacherId或subject参数"));
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("exams", exams);
        data.put("total", exams.size());
        return ResponseEntity.ok(ApiResponse.success("获取成功", data));
    }
}

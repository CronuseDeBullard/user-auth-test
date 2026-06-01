package com.softwaretest.controller;

import com.softwaretest.dto.ApiResponse;
import com.softwaretest.entity.ExamRecord;
import com.softwaretest.service.ExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    
    private final ExamService examService;
    
    @PostMapping("/exams/{examId}/submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitExam(
            @PathVariable Long examId,
            @RequestParam Long studentId,
            @RequestParam Double score) {
        
        try {
            ExamRecord record = examService.submitExam(examId, studentId, score);
            Map<String, Object> data = new HashMap<>();
            data.put("recordId", record.getId());
            data.put("score", record.getScore());
            data.put("status", record.getStatus());
            return ResponseEntity.ok(ApiResponse.success("提交成功", data));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, e.getMessage()));
        }
    }
    
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRecords(
            @RequestParam Long studentId) {
        
        List<ExamRecord> records = examService.getStudentRecords(studentId);
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", records.size());
        return ResponseEntity.ok(ApiResponse.success("获取成功", data));
    }
    
    @GetMapping("/exams/{examId}/result")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResult(
            @PathVariable Long examId,
            @RequestParam Long studentId) {
        
        ExamRecord record = examService.getExamRecord(examId, studentId);
        if (record == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(400, "未找到考试记录"));
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("score", record.getScore());
        data.put("status", record.getStatus());
        data.put("submitTime", record.getSubmitTime());
        return ResponseEntity.ok(ApiResponse.success("获取成功", data));
    }
}

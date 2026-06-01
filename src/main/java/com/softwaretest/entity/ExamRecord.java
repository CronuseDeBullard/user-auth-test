package com.softwaretest.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "exam_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "exam_id", nullable = false)
    private Long examId;
    
    @Column(name = "student_id", nullable = false)
    private Long studentId;
    
    @Column(name = "score")
    private Double score;
    
    @Column(name = "status")
    private Integer status; // 0:未提交 1:已提交 2:已批改
    
    @Column(name = "submit_time")
    private LocalDateTime submitTime;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = 0;
        }
    }
}

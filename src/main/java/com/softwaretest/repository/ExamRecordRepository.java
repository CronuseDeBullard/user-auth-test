package com.softwaretest.repository;

import com.softwaretest.entity.ExamRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRecordRepository extends JpaRepository<ExamRecord, Long> {
    
    Optional<ExamRecord> findByExamIdAndStudentId(Long examId, Long studentId);
    
    List<ExamRecord> findByStudentId(Long studentId);
    
    List<ExamRecord> findByExamId(Long examId);
    
    long countByExamIdAndStatus(Long examId, Integer status);
}

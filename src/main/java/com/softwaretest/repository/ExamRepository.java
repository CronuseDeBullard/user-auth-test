package com.softwaretest.repository;

import com.softwaretest.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    
    List<Exam> findByTeacherId(Long teacherId);
    
    List<Exam> findBySubject(String subject);
    
    List<Exam> findByStatus(Integer status);
    
    long countByTeacherId(Long teacherId);
}

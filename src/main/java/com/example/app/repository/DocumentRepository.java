package com.example.app.repository;

import com.example.app.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByUserId(Long userId);
    
    List<Document> findByUserIdAndCategory(Long userId, String category);
    
    List<Document> findByUserIdAndProcessed(Long userId, Boolean processed);
    
    boolean existsByFilenameAndUserId(String filename, Long userId);
}

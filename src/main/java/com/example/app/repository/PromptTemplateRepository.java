package com.example.app.repository;

import com.example.app.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
    
    Optional<PromptTemplate> findByName(String name);
    
    List<PromptTemplate> findByCategory(String category);
    
    boolean existsByName(String name);
}

package com.example.app.repository;

import com.example.app.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByConversationId(String conversationId);

    List<ChatMessage> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByConversationId(String conversationId);

    void deleteByUserId(Long userId);

    @Query("SELECT DISTINCT c.conversationId FROM ChatMessage c WHERE c.userId = :userId")
    List<String> findDistinctConversationIdsByUserId(@Param("userId") Long userId);
}
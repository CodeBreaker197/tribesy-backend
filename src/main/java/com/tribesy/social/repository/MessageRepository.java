package com.tribesy.social.repository;

import com.tribesy.social.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatIdOrderByCreatedAtAsc(Long chatId);

    @Query("SELECT DISTINCT m.chatId FROM Message m WHERE m.senderId = :userId")
    List<Long> findDistinctChatIdsByUserId(@Param("userId") Long userId);

    Optional<Message> findFirstByChatIdOrderByCreatedAtDesc(Long chatId);
}
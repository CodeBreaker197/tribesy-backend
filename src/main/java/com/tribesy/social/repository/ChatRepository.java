package com.tribesy.social.repository;

import com.tribesy.social.entity.Chat;
import com.tribesy.social.entity.ChatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("""
    SELECT c FROM Chat c 
    JOIN ChatParticipant cp1 ON c.id = cp1.chatId 
    JOIN ChatParticipant cp2 ON c.id = cp2.chatId 
    WHERE c.type = :type 
    AND cp1.userId = :userId1 
    AND cp2.userId = :userId2
    """)
    Optional<Chat> findDirectChatBetween(@Param("userId1") Long userId1,
                                         @Param("userId2") Long userId2,
                                         @Param("type") ChatType type);

    @Query("""
    SELECT DISTINCT c FROM Chat c 
    JOIN ChatParticipant cp ON c.id = cp.chatId 
    WHERE cp.userId = :userId
    """)
    List<Chat> findAllByUserId(@Param("userId") Long userId);
}
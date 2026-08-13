package com.tribesy.social.repository;

import com.tribesy.social.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderAndRecipientOrSenderAndRecipientOrderByCreatedAtAsc(
            String sender1, String recipient1, String sender2, String recipient2
    );
}
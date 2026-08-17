package com.tribesy.social.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    private Long id;
    private String senderUsername;
    private String recipientUsername;
    private String content;
    private String timestamp;
}
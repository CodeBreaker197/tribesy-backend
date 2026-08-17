package com.tribesy.social.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatDto {
    private Long id;
    private UserDto participant;
    private String lastMessage;
    private String updatedAt;
}
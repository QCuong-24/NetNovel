package com.example.netnovel_server.chatbot.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChatbotAdminTestRequestDTO {

    private String message;

    private String language;

    private List<String> roles;
}

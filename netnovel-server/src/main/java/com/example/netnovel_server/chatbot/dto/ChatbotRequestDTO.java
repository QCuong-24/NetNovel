package com.example.netnovel_server.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatbotRequestDTO {

    @NotBlank(message = "Chatbot message is required")
    @Size(max = 1000, message = "Chatbot message must not exceed 1000 characters")
    private String message;

    @Size(max = 16, message = "Language must not exceed 16 characters")
    private String language;
}

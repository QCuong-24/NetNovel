package com.example.netnovel_server.chatbot.dto;

import com.example.netnovel_server.dto.NovelDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotResponseDTO {

    private String reply;

    private String language;

    private String intent;

    private double confidence;

    private List<NovelDTO> novels;

    private List<String> suggestedQuestions;

    private List<ChatbotActionDTO> actions;
}

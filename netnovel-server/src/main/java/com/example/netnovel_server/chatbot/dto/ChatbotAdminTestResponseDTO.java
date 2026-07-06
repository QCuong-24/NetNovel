package com.example.netnovel_server.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotAdminTestResponseDTO {

    private String message;

    private String requestedLanguage;

    private List<String> testRoles;

    private String detectedLanguage;

    private String intent;

    private double confidence;

    private double secondBestConfidence;

    private boolean ambiguous;

    private String clarificationType;

    private Map<String, String> filters;

    private String faqId;

    private String intentType;

    private String responseType;

    private boolean semanticTried;

    private boolean semanticUsed;

    private boolean permissionDenied;

    private String searchMode;

    private Integer novelResultCount;

    private ChatbotResponseDTO response;

    private List<String> actionValues;
}

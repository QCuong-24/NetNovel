package com.example.netnovel_server.chatbot.model;

public enum ChatbotLanguage {
    VI("vi"),
    EN("en");

    private final String code;

    ChatbotLanguage(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}

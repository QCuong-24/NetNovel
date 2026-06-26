package com.example.netnovel_server.chatbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_intent_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotIntentDefinition {

    @Id
    @Column(length = 128)
    private String id;

    @Column(length = 64)
    private String type;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String examplesJson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String repliesJson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String filtersJson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String tagsJson;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String actionsJson;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    private void beforeCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        setDefaults();
    }

    @PreUpdate
    private void beforeUpdate() {
        updatedAt = LocalDateTime.now();
        setDefaults();
    }

    private void setDefaults() {
        if (enabled == null) {
            enabled = true;
        }
        if (priority == null) {
            priority = 0;
        }
    }
}

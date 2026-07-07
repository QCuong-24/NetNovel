package com.example.netnovel_server.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NovelLastReadUpdateDTO {

    @NotNull(message = "Novel id is required")
    @Positive(message = "Novel id must be positive")
    private Long novelId;

    @NotNull(message = "Chapter id is required")
    @Positive(message = "Chapter id must be positive")
    private Long chapterId;
}

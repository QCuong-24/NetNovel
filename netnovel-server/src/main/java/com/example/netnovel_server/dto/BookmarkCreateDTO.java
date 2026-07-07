package com.example.netnovel_server.dto;

import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookmarkCreateDTO {

    @Positive(message = "Novel id must be positive")
    private Long novelId;

    @Positive(message = "Chapter id must be positive")
    private Long chapterId;
}

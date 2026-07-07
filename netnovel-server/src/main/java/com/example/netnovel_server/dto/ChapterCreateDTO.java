package com.example.netnovel_server.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterCreateDTO {

    @NotBlank(message = "Chapter title is required")
    @Size(max = 255, message = "Chapter title must not exceed 255 characters")
    private String title;

    @NotNull(message = "Chapter number is required")
    @Min(value = 1, message = "Chapter number must be at least 1")
    private Integer chapterNumber;

    @NotBlank(message = "Chapter content is required")
    private String content;
}

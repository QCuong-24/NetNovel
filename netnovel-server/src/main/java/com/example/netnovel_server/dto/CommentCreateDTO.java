package com.example.netnovel_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentCreateDTO {

    @NotBlank(message = "Comment content is required")
    @Size(max = 5000, message = "Comment content must not exceed 5000 characters")
    private String content;
}

package com.example.netnovel_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlTaskCreateDTO {

    @NotBlank(message = "Crawl URL is required")
    @Size(max = 2000, message = "Crawl URL must not exceed 2000 characters")
    private String url;
}

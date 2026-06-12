package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.CrawlTaskDTO;
import com.example.netnovel_server.entity.CrawlTask;
import com.example.netnovel_server.entity.User;

public final class CrawlTaskMapper {

    private CrawlTaskMapper() {
    }

    public static CrawlTaskDTO toDTO(CrawlTask task) {
        return CrawlTaskDTO.builder()
            .id(task.getId())
            .url(task.getUrl())
            .status(task.getStatus().name())
            .requestedByUserId(task.getRequestedBy() == null ? null : task.getRequestedBy().getId())
            .errorMessage(task.getErrorMessage())
            .startedAt(task.getStartedAt())
            .finishedAt(task.getFinishedAt())
            .createAt(task.getCreateAt())
            .updateAt(task.getUpdateAt())
            .build();
    }
}

package com.example.netnovel_server.event;

public record CrawlTaskCreatedEvent(
    Long taskId,
    String url,
    Long requestedByUserId
) {
}

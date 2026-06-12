package com.example.netnovel_server.entity;

public enum CrawlTaskStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    PARTIAL_SUCCESS,
    FAILED,
    SKIPPED_UNSUPPORTED_SOURCE,
    CANCELLED
}

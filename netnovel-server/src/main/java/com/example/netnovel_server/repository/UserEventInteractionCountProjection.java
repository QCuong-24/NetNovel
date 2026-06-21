package com.example.netnovel_server.repository;

public interface UserEventInteractionCountProjection {

    Long getEntityId();

    long getDistinctCount();
}

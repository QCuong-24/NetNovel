package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.UserEventType;

public interface UserEventTypeCountProjection {

    UserEventType getEventType();

    long getEventCount();
}

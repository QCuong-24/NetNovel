package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Novel;

public interface NovelFollowCount {

    Novel getNovel();

    Long getFollowCount();
}

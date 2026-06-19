package com.example.netnovel_server.repository;

import com.example.netnovel_server.entity.Novel;

public interface CommentNovelCount {

    Novel getNovel();

    Long getCommentCount();
}

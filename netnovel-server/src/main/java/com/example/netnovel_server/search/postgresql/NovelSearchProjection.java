package com.example.netnovel_server.search.postgresql;

public interface NovelSearchProjection {

    Long getNovelId();

    Double getScore();
}

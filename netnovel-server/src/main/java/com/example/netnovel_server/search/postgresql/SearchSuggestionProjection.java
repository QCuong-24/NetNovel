package com.example.netnovel_server.search.postgresql;

public interface SearchSuggestionProjection {

    String getType();

    Long getId();

    String getLabel();
}

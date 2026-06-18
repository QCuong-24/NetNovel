package com.example.netnovel_server.mapper;

import com.example.netnovel_server.dto.GenreDTO;
import com.example.netnovel_server.entity.Genre;

public final class GenreMapper {

    private GenreMapper() {
    }

    public static GenreDTO toDTO(Genre genre) {
        if (genre == null) {
            return null;
        }

        return GenreDTO.builder()
            .genreId(genre.getId())
            .name(genre.getName())
            .build();
    }

    public static Genre toEntity(GenreDTO dto) {
        if (dto == null) {
            return null;
        }

        return Genre.builder()
            .id(dto.getGenreId())
            .name(dto.getName())
            .build();
    }
}

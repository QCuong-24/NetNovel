package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.GenreDTO;
import com.example.netnovel_server.entity.Genre;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.GenreMapper;
import com.example.netnovel_server.repository.GenreRepository;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.utility.TextUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class GenreService {

    private final GenreRepository genreRepository;
    private final NovelRepository novelRepository;

    public GenreService(GenreRepository genreRepository, NovelRepository novelRepository) {
        this.genreRepository = genreRepository;
        this.novelRepository = novelRepository;
    }

    @Transactional(readOnly = true)
    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAllByOrderByNameAsc().stream()
            .map(GenreMapper::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public GenreDTO getGenre(Long genreId) {
        return GenreMapper.toDTO(findGenre(genreId));
    }

    @Transactional
    public GenreDTO createGenre(GenreDTO request) {
        String name = normalizeName(request.getName());

        if (genreRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Genre already exists");
        }

        Genre genre = Genre.builder()
            .name(name)
            .build();

        return GenreMapper.toDTO(genreRepository.save(genre));
    }

    @Transactional
    public List<GenreDTO> createGenres(List<String> genreNames) {
        if (genreNames == null || genreNames.isEmpty()) {
            throw new BadRequestException("Genre list is required");
        }

        Set<String> names = new LinkedHashSet<>();
        for (String genreName : genreNames) {
            names.add(normalizeName(genreName));
        }

        List<Genre> genres = names.stream()
            .map(name -> genreRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> genreRepository.save(Genre.builder().name(name).build())))
            .toList();

        return genres.stream()
            .map(GenreMapper::toDTO)
            .toList();
    }

    @Transactional
    public void deleteGenre(Long genreId) {
        Genre genre = findGenre(genreId);
        List<Novel> novels = novelRepository.findByGenresId(genreId);

        for (Novel novel : novels) {
            novel.getGenres().remove(genre);
        }
        novelRepository.saveAll(novels);
        genreRepository.delete(genre);
    }

    private Genre findGenre(Long genreId) {
        return genreRepository.findById(genreId)
            .orElseThrow(() -> new ResourceNotFoundException("Genre not found"));
    }

    private String normalizeName(String name) {
        String normalized = TextUtils.toTitleCaseWords(name);
        if (normalized == null || normalized.isBlank()) {
            throw new BadRequestException("Genre name is required");
        }
        return normalized;
    }
}

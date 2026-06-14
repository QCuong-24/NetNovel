package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.GenreDTO;
import com.example.netnovel_server.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@Tag(name = "Genres", description = "Genre catalog APIs")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    @Operation(summary = "Get all genres")
    public ResponseEntity<List<GenreDTO>> getAllGenres() {
        return ResponseEntity.ok(genreService.getAllGenres());
    }

    @GetMapping("/{genreId}")
    @Operation(summary = "Get a genre by id")
    public ResponseEntity<GenreDTO> getGenre(@PathVariable Long genreId) {
        return ResponseEntity.ok(genreService.getGenre(genreId));
    }

    @PostMapping
    @Operation(summary = "Create a genre")
    public ResponseEntity<GenreDTO> createGenre(@RequestBody GenreDTO request) {
        return ResponseEntity.ok(genreService.createGenre(request));
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple genres from a string list")
    public ResponseEntity<List<GenreDTO>> createGenres(@RequestBody List<String> genreNames) {
        return ResponseEntity.ok(genreService.createGenres(genreNames));
    }

    @DeleteMapping("/{genreId}")
    @Operation(summary = "Delete a genre and remove it from related novels")
    public ResponseEntity<Void> deleteGenre(@PathVariable Long genreId) {
        genreService.deleteGenre(genreId);
        return ResponseEntity.noContent().build();
    }
}

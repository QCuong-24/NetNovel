package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.TagDTO;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.Tag;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.mapper.TagMapper;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.repository.TagRepository;
import com.example.netnovel_server.utility.TextUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final NovelRepository novelRepository;

    public TagService(TagRepository tagRepository, NovelRepository novelRepository) {
        this.tagRepository = tagRepository;
        this.novelRepository = novelRepository;
    }

    @Transactional(readOnly = true)
    public List<TagDTO> getAllTags() {
        return tagRepository.findAllByOrderByNameAsc().stream()
            .map(TagMapper::toDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public TagDTO getTag(Long tagId) {
        return TagMapper.toDTO(findTag(tagId));
    }

    @Transactional
    public TagDTO createTag(TagDTO request) {
        String name = normalizeName(request.getName());

        if (tagRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("Tag already exists");
        }

        Tag tag = Tag.builder()
            .name(name)
            .build();

        return TagMapper.toDTO(tagRepository.save(tag));
    }

    @Transactional
    public List<TagDTO> createTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            throw new BadRequestException("Tag list is required");
        }

        Set<String> names = new LinkedHashSet<>();
        for (String tagName : tagNames) {
            names.add(normalizeName(tagName));
        }

        List<Tag> tags = names.stream()
            .map(name -> tagRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build())))
            .toList();

        return tags.stream()
            .map(TagMapper::toDTO)
            .toList();
    }

    @Transactional
    public void deleteTag(Long tagId) {
        Tag tag = findTag(tagId);
        List<Novel> novels = novelRepository.findByTagsId(tagId);

        for (Novel novel : novels) {
            novel.getTags().remove(tag);
        }
        novelRepository.saveAll(novels);
        tagRepository.delete(tag);
    }

    private Tag findTag(Long tagId) {
        return tagRepository.findById(tagId)
            .orElseThrow(() -> new ResourceNotFoundException("Tag not found"));
    }

    private String normalizeName(String name) {
        String normalized = TextUtils.toTitleCaseWords(name);
        if (normalized == null || normalized.isBlank()) {
            throw new BadRequestException("Tag name is required");
        }
        return normalized;
    }
}

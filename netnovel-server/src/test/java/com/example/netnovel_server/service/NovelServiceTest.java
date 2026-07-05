package com.example.netnovel_server.service;

import com.example.netnovel_server.dto.NovelCreateDTO;
import com.example.netnovel_server.dto.NovelDTO;
import com.example.netnovel_server.entity.AuthProvider;
import com.example.netnovel_server.entity.Genre;
import com.example.netnovel_server.entity.Novel;
import com.example.netnovel_server.entity.NovelAccessStatus;
import com.example.netnovel_server.entity.NovelFollow;
import com.example.netnovel_server.entity.Role;
import com.example.netnovel_server.entity.Status;
import com.example.netnovel_server.entity.Tag;
import com.example.netnovel_server.entity.User;
import com.example.netnovel_server.exception.BadRequestException;
import com.example.netnovel_server.exception.DuplicateResourceException;
import com.example.netnovel_server.exception.ResourceNotFoundException;
import com.example.netnovel_server.repository.GenreRepository;
import com.example.netnovel_server.repository.NovelFollowRepository;
import com.example.netnovel_server.repository.NovelRepository;
import com.example.netnovel_server.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NovelServiceTest {

    /*
     * Unit scope:
     * - NovelService validation, genre/tag resolution, create/update/delete behavior.
     * - Repositories, notifications, and chapter-info refresh are mocked.
     * - Search/list read methods can be covered separately with repository mapping tests.
     */

    @Mock
    private NovelRepository novelRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private NovelFollowRepository novelFollowRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NovelChapterInfoService novelChapterInfoService;

    private NovelService novelService;

    @BeforeEach
    void setUp() {
        novelService = new NovelService(
            novelRepository,
            genreRepository,
            tagRepository,
            novelFollowRepository,
            notificationService,
            novelChapterInfoService
        );
    }

    // Create flow: a new novel is saved with resolved genres and tags, and the chapter info is refreshed.
    @Test
    void createNovelResolvesGenresAndTagsBeforeSaving() {
        NovelCreateDTO request = request("Lord of Tests", "ONGOING", "PREVIEW_ONLY");
        Genre fantasy = genre(1L, "Fantasy");
        Tag cultivation = tag(2L, "Cultivation");

        when(novelRepository.existsByTitleIgnoreCase(request.getTitle())).thenReturn(false);
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.of(fantasy));
        when(tagRepository.findByNameIgnoreCase("Cultivation")).thenReturn(Optional.of(cultivation));
        when(novelRepository.save(any(Novel.class))).thenAnswer(invocation -> {
            Novel novel = invocation.getArgument(0);
            novel.setId(100L);
            return novel;
        });

        NovelDTO response = novelService.createNovel(request);

        assertEquals(100L, response.getNovelId());
        assertEquals(request.getTitle(), response.getTitle());
        assertEquals("ONGOING", response.getStatus());
        assertEquals("PREVIEW_ONLY", response.getAccessStatus());
        assertTrue(response.getGenres().contains("Fantasy"));

        ArgumentCaptor<Novel> novelCaptor = ArgumentCaptor.forClass(Novel.class);
        verify(novelRepository).save(novelCaptor.capture());
        Novel novelToSave = novelCaptor.getValue();
        assertEquals(Status.ONGOING, novelToSave.getStatus());
        assertEquals(NovelAccessStatus.PREVIEW_ONLY, novelToSave.getAccessStatus());
        assertTrue(novelToSave.getGenres().contains(fantasy));
        assertTrue(novelToSave.getTags().contains(cultivation));
        verify(novelChapterInfoService).refresh(100L);
    }

    // Create guards: duplicate titles are rejected before any genre/tag resolution or saving.
    @Test
    void createNovelRejectsDuplicateTitleBeforeResolvingGenres() {
        NovelCreateDTO request = request("Lord of Tests", "ONGOING", "NORMAL");

        when(novelRepository.existsByTitleIgnoreCase(request.getTitle())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> novelService.createNovel(request));

        verify(genreRepository, never()).findByNameIgnoreCase(any());
        verify(tagRepository, never()).findByNameIgnoreCase(any());
        verify(novelRepository, never()).save(any());
    }

    // Create guards: unknown genres are rejected before any saving.
    @Test
    void createNovelRejectsUnknownGenre() {
        NovelCreateDTO request = request("Lord of Tests", "ONGOING", "NORMAL");

        when(novelRepository.existsByTitleIgnoreCase(request.getTitle())).thenReturn(false);
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> novelService.createNovel(request));

        verify(novelRepository, never()).save(any());
    }

    // Create guards: unknown tags are rejected before any saving.
    @Test
    void createNovelRejectsInvalidStatus() {
        NovelCreateDTO request = request("Lord of Tests", "not-a-status", "NORMAL");

        when(novelRepository.existsByTitleIgnoreCase(request.getTitle())).thenReturn(false);
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.of(genre(1L, "Fantasy")));
        when(tagRepository.findByNameIgnoreCase("Cultivation")).thenReturn(Optional.of(tag(2L, "Cultivation")));

        assertThrows(BadRequestException.class, () -> novelService.createNovel(request));

        verify(novelRepository, never()).save(any());
    }

    // Update flow: an existing novel is mutated with resolved genres and tags, and the chapter info is refreshed.
    @Test
    void updateNovelMutatesExistingNovelAndResolvesRelationships() {
        Long novelId = 100L;
        Novel existingNovel = novel(novelId, "Old Title", Status.ONGOING, NovelAccessStatus.NORMAL);
        NovelCreateDTO request = request("New Title", "COMPLETED", "PREVIEW_ONLY");
        Genre fantasy = genre(1L, "Fantasy");
        Tag cultivation = tag(2L, "Cultivation");

        when(novelRepository.findById(novelId)).thenReturn(Optional.of(existingNovel));
        when(novelRepository.existsByTitleIgnoreCaseAndIdNot(request.getTitle(), novelId)).thenReturn(false);
        when(genreRepository.findByNameIgnoreCase("Fantasy")).thenReturn(Optional.of(fantasy));
        when(tagRepository.findByNameIgnoreCase("Cultivation")).thenReturn(Optional.of(cultivation));
        when(novelRepository.save(existingNovel)).thenReturn(existingNovel);

        NovelDTO response = novelService.updateNovel(novelId, request);

        assertEquals("New Title", response.getTitle());
        assertEquals("COMPLETED", response.getStatus());
        assertEquals("PREVIEW_ONLY", response.getAccessStatus());
        assertEquals(request.getAuthor(), existingNovel.getAuthor());
        assertEquals(request.getDescription(), existingNovel.getDescription());
        assertEquals(Status.COMPLETED, existingNovel.getStatus());
        assertEquals(NovelAccessStatus.PREVIEW_ONLY, existingNovel.getAccessStatus());
        assertTrue(existingNovel.getGenres().contains(fantasy));
        assertTrue(existingNovel.getTags().contains(cultivation));
        verify(novelRepository).save(existingNovel);
    }

    // Update guards: updating a non-existent novel is rejected before any mutation or saving.
    @Test
    void updateNovelRejectsMissingNovel() {
        when(novelRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> novelService.updateNovel(404L, request("Missing", "ONGOING", "NORMAL")));

        verify(novelRepository, never()).save(any());
    }

    // Delete flow: deleting a novel notifies followers before removing the novel from the repository.
    @Test
    void deleteNovelNotifiesFollowersBeforeDeletingNovel() {
        Long novelId = 100L;
        Novel novel = novel(novelId, "Lord of Tests", Status.ONGOING, NovelAccessStatus.NORMAL);
        User follower = user(20L);
        NovelFollow follow = NovelFollow.builder()
            .novel(novel)
            .user(follower)
            .build();

        when(novelRepository.findById(novelId)).thenReturn(Optional.of(novel));
        when(novelFollowRepository.findByNovelId(novelId)).thenReturn(List.of(follow));

        novelService.deleteNovel(novelId);

        verify(notificationService).createNotifications(
            List.of(follower),
            NotificationService.TYPE_NOVEL_DELETED,
            "Novel deleted: Lord of Tests",
            "A novel you followed has been deleted.",
            "/api/novels"
        );
        verify(novelRepository).delete(novel);
    }

    private static NovelCreateDTO request(String title, String status, String accessStatus) {
        return NovelCreateDTO.builder()
            .title(title)
            .author("Test Author")
            .description("A description long enough for service tests.")
            .coverImageUrl("https://example.com/cover.png")
            .coverImagePublicId("cover-public-id")
            .genres(Set.of("Fantasy"))
            .tags(Set.of("Cultivation"))
            .status(status)
            .accessStatus(accessStatus)
            .build();
    }

    private static Novel novel(Long id, String title, Status status, NovelAccessStatus accessStatus) {
        return Novel.builder()
            .id(id)
            .title(title)
            .author("Original Author")
            .description("Original description")
            .status(status)
            .accessStatus(accessStatus)
            .build();
    }

    private static Genre genre(Long id, String name) {
        return Genre.builder()
            .id(id)
            .name(name)
            .build();
    }

    private static Tag tag(Long id, String name) {
        return Tag.builder()
            .id(id)
            .name(name)
            .build();
    }

    private static User user(Long id) {
        return User.builder()
            .id(id)
            .username("reader")
            .email("reader@example.com")
            .provider(AuthProvider.LOCAL)
            .roles(Set.of(Role.USER))
            .build();
    }
}

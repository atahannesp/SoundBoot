package com.example.soundboot.service.playlistService;

import com.example.soundboot.dto.request.CreatePlaylistRequest;
import com.example.soundboot.dto.response.ListPlaylistResponse;
import com.example.soundboot.entity.PlaylistEntity;
import com.example.soundboot.entity.SongEntity;
import com.example.soundboot.entity.UserEntity;
import com.example.soundboot.repository.PlaylistRepository;
import com.example.soundboot.repository.SongRepository;
import com.example.soundboot.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceImplementationTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private SongRepository songRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PlaylistServiceImplementation playlistService;

    private UserEntity userEntity;
    private SongEntity songEntity;
    private PlaylistEntity playlistEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setUsername("testuser");

        songEntity = new SongEntity();
        songEntity.setId(UUID.randomUUID());
        songEntity.setTitle("Test Song");
        songEntity.setArtist("Test Artist");
        songEntity.setPlayCount(10);
        songEntity.setStorageKey("test/key");

        playlistEntity = new PlaylistEntity();
        playlistEntity.setId(UUID.randomUUID());
        playlistEntity.setName("Test Playlist");
        playlistEntity.setUser(userEntity);
        
        List<SongEntity> songs = new ArrayList<>();
        songs.add(songEntity);
        playlistEntity.setSongs(songs);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String username) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(username);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createPlaylist_Success() {
        mockSecurityContext("testuser");

        UUID songId = songEntity.getId();
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("New Playlist");
        request.setPublic(true);
        request.setSongs(List.of(songId));

        when(songRepository.findAllById(request.getSongs())).thenReturn(List.of(songEntity));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));

        playlistService.createPlaylist(request);

        ArgumentCaptor<PlaylistEntity> playlistCaptor = ArgumentCaptor.forClass(PlaylistEntity.class);
        verify(playlistRepository, times(1)).save(playlistCaptor.capture());
        
        PlaylistEntity savedPlaylist = playlistCaptor.getValue();
        assertEquals("New Playlist", savedPlaylist.getName());
        assertEquals("New Playlist", songEntity.getPlaylist().getName());
    }

    @Test
    void createPlaylist_ThrowsException_WhenSongNotFound() {
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setName("New Playlist");
        request.setSongs(List.of(UUID.randomUUID()));

        when(songRepository.findAllById(request.getSongs())).thenReturn(new ArrayList<>());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> playlistService.createPlaylist(request));
        assertEquals("Some songs could not be found.", exception.getMessage());
    }

    @Test
    void deletePlaylist_Success() {
        mockSecurityContext("testuser");

        UUID playlistId = playlistEntity.getId();
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlistEntity));

        playlistService.deletePlaylist(playlistId);

        verify(playlistRepository, times(1)).delete(playlistEntity);
    }

    @Test
    void deletePlaylist_ThrowsAccessDeniedException_WhenNotOwner() {
        mockSecurityContext("wronguser");

        UUID playlistId = playlistEntity.getId();
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlistEntity));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> playlistService.deletePlaylist(playlistId));
        assertEquals("You do not have permission to delete this playlist.", exception.getMessage());
    }

    @Test
    void listPlaylist_Success() {
        UUID playlistId = playlistEntity.getId();
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlistEntity));

        List<ListPlaylistResponse> responses = playlistService.listPlaylist(playlistId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Test Song", responses.getFirst().getTitle());
    }
}

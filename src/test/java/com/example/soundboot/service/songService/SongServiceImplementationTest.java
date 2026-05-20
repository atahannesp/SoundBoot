package com.example.soundboot.service.songService;

import com.example.soundboot.entity.SongEntity;
import com.example.soundboot.entity.UserEntity;
import com.example.soundboot.repository.SongRepository;
import com.example.soundboot.repository.UserRepository;
import com.example.soundboot.service.segmentationService.AudioSegmentationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SongServiceImplementationTest {

    @Mock
    private AudioSegmentationService audioSegmentService;

    @Mock
    private SongRepository songRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SongServiceImplementation songService;

    private UserEntity userEntity;
    private SongEntity songEntity;
    private UUID songId;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setUsername("testuser");

        songId = UUID.randomUUID();
        songEntity = new SongEntity();
        songEntity.setId(songId);
        songEntity.setTitle("Test Song");
        songEntity.setUser(userEntity);
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
    void uploadSong_Success() throws Exception {
        mockSecurityContext("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userEntity));
        when(songRepository.save(any(SongEntity.class))).thenReturn(songEntity);

        Path dummyPath = Paths.get("dummy/path");
        when(audioSegmentService.segment(any(MultipartFile.class), eq(songId.toString()))).thenReturn(dummyPath);

        MockMultipartFile file = new MockMultipartFile("file", "test.mp3", "audio/mpeg", "dummy content".getBytes());

        songService.uploadSong(file, "Test Song");

        verify(songRepository, times(2)).save(any(SongEntity.class)); // 1 for initial save, 1 for updating storageKey
        verify(audioSegmentService, times(1)).segment(any(MultipartFile.class), eq(songId.toString()));
        assertEquals("uploads/hls/" + songId.toString() + "/index.m3u8", songEntity.getStorageKey());
    }

    @Test
    void deleteSong_Success() throws Exception {
        mockSecurityContext("testuser");
        when(songRepository.findById(songId)).thenReturn(Optional.of(songEntity));

        songService.deleteSong(songId);

        verify(audioSegmentService, times(1)).deleteSegments(songId.toString());
        verify(songRepository, times(1)).delete(songEntity);
    }

    @Test
    void deleteSong_ThrowsAccessDeniedException_WhenNotOwner() {
        mockSecurityContext("wronguser");
        when(songRepository.findById(songId)).thenReturn(Optional.of(songEntity));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> songService.deleteSong(songId));
        assertEquals("You do not have permission to delete this song.", exception.getMessage());
        
        verify(songRepository, never()).delete(any(SongEntity.class));
    }

    @Test
    void deleteSong_ThrowsResourceNotFoundException_WhenSongNotFound() {
        mockSecurityContext("testuser");
        when(songRepository.findById(songId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> songService.deleteSong(songId));
        assertTrue(exception.getMessage().contains("Song not found with ID"));
    }
}

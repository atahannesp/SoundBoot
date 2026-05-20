package com.example.soundboot.service.streamService;

import com.example.soundboot.entity.SongEntity;
import com.example.soundboot.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreamServiceImplementationTest {

    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private StreamServiceImplementation streamService;

    private UUID trackId;
    private SongEntity songEntity;
    private Path tempHlsDir;

    @BeforeEach
    void setUp() throws Exception {
        trackId = UUID.randomUUID();
        songEntity = new SongEntity();
        songEntity.setId(trackId);
        songEntity.setPlayCount(5);

        // Create a temporary directory structure for testing HLS file serving
        tempHlsDir = Paths.get("uploads/hls/" + trackId);
        Files.createDirectories(tempHlsDir);
    }

    @Test
    void getHlsFile_Success() throws Exception {
        String fileName = "index.m3u8";
        Path testFile = tempHlsDir.resolve(fileName);
        Files.writeString(testFile, "dummy m3u8 content");

        when(songRepository.findById(trackId)).thenReturn(Optional.of(songEntity));

        Resource resource = streamService.getHlsFile(trackId.toString(), fileName);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals(fileName, resource.getFilename());
        verify(songRepository, times(1)).save(songEntity); // playCount increment
        assertEquals(6, songEntity.getPlayCount());

        Files.deleteIfExists(testFile);
        Files.deleteIfExists(tempHlsDir);
    }

    @Test
    void getHlsFile_ThrowsException_OnPathTraversal() {
        String invalidFileName = "../index.m3u8";

        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            streamService.getHlsFile(trackId.toString(), invalidFileName)
        );

        assertEquals("Error occurred while reading the file.", exception.getMessage());
    }
}

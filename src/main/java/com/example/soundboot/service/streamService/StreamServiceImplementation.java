package com.example.soundboot.service.streamService;

import com.example.soundboot.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Implementation of the StreamService interface.
 * Handles serving HLS files .
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StreamServiceImplementation implements StreamService {

    private static final String BASE_DIR = "uploads/hls/";
    private final SongRepository songRepository;

    /**
     * Retrieves an HLS-related file (.m3u8 or .ts) for a specific track.
     * Prevents path traversal attacks and increments the play count when
     * the master playlist (.m3u8) is requested.
     *
     * @param trackId  The unique identifier of the track.
     * @param fileName The name of the file to be streamed (e.g., index.m3u8 or seg000.ts).
     * @return A {@link Resource} representing the requested file.
     * @throws RuntimeException if the file path is invalid, the file cannot be found, or an error occurs during retrieval.
     */
    @Override
    public Resource getHlsFile(String trackId, String fileName) {
        try {
            if (trackId.contains("..") || fileName.contains("..")) {
                throw new RuntimeException("Invalid file path request.");
            }

            if (fileName.endsWith(".m3u8")) {
                incrementPlayCount(trackId);
            }

            Path filePath = Paths.get(BASE_DIR, trackId, fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable.");
            }
        } catch (Exception e) {
            log.error("Error reading file in stream service: ", e);
            throw new RuntimeException("Error occurred while reading the file.");
        }
    }

    /**
     * Increments the play count of a track by finding it in the database and updating its value.
     * If the trackId is invalid or a database error occurs, it merely logs a warning to avoid interrupting the stream.
     *
     * @param trackId The unique identifier of the track to be updated.
     */
    private void incrementPlayCount(String trackId) {
        try {
            UUID id = UUID.fromString(trackId);
            songRepository.findById(id).ifPresent(song -> {
                song.setPlayCount(song.getPlayCount() + 1);
                songRepository.save(song);
            });
        } catch (Exception e) {
            log.warn("Could not increment play count, invalid trackId: {}", trackId);
        }
    }
}

package com.example.soundboot.service.songService;

import com.example.soundboot.entity.SongEntity;
import com.example.soundboot.entity.UserEntity;
import com.example.soundboot.repository.SongRepository;
import com.example.soundboot.repository.UserRepository;
import com.example.soundboot.service.segmentationService.AudioSegmentationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Implementation of the SongService interface.
 * Handles song uploads, deletions, and other song-related operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SongServiceImplementation implements SongService {

    private final AudioSegmentationService audioSegmentService;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    /**
     * Uploads a new song, segments it into HLS format, and saves its metadata to the database.
     * Requires an authenticated user context.
     *
     * @param file  the audio file to be uploaded.
     * @param title the title of the song.
     * @throws IOException if an I/O error occurs during file processing or segmentation.
     * @throws ResourceNotFoundException if the currently authenticated user cannot be found in the database.
     */
    @Override
    public void uploadSong(MultipartFile file, String title) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String currentUsername = authentication.getName();
        UserEntity user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUsername));


        SongEntity newSong = SongEntity.builder()
                .title(title)
                .user(user)
                .artist(user.getUsername())
                .playCount(0)
                .storageKey("")
                .build();

        SongEntity savedSong = songRepository.save(newSong);

        String trackId = savedSong.getId().toString();
        audioSegmentService.segment(file, trackId);

        savedSong.setStorageKey("uploads/hls/" + trackId + "/index.m3u8");
        songRepository.save(savedSong);
    }

    /**
     * Deletes a song, including its physical files and database record.
     * Ensures that only the user who uploaded the song can delete it.
     *
     * @param id the UUID of the song to be deleted.
     * @throws IOException if an I/O error occurs during physical file deletion.
     * @throws ResourceNotFoundException if the song cannot be found in the database.
     * @throws AccessDeniedException if the user attempting to delete is not the owner of the song.
     */
    @Override
    public void deleteSong(UUID id) throws IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String currentUsername = authentication.getName();

        SongEntity song = songRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found with ID: " + id));

        if (!song.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not have permission to delete this song.");
        }

        audioSegmentService.deleteSegments(id.toString());
        songRepository.delete(song);
    }
}

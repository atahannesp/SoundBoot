package com.example.soundboot.controller;

import com.example.soundboot.service.songService.SongService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

/**
 * Controller class for managing song-related operations.
 * Provides endpoints for uploading new songs and deleting existing ones.
 */
@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@Slf4j
public class SongController {

    private final SongService songService;

    /**
     * Endpoint to upload a new song.
     * The uploaded file will be segmented for HLS streaming, and its metadata will be saved.
     *
     * @param file  The audio file (e.g., MP3) to be uploaded as a {@link MultipartFile}.
     * @param title The title of the song provided by the user.
     * @return A {@link ResponseEntity} with a success message if the upload and processing succeed,
     *         or an error message with a 500 INTERNAL_SERVER_ERROR status if it fails.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadSong(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) {

        try {
            songService.uploadSong(file, title);
            return ResponseEntity.ok("Song successfully uploaded and processed!");

        } catch (Exception e) {
            log.error("An error occurred while uploading the song: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Song could not be uploaded: " + e.getMessage());
        }
    }

    /**
     * Endpoint to delete an existing song by its unique identifier.
     * Also deletes the associated HLS segment files from the server.
     * Only the user who uploaded the song is authorized to delete it.
     *
     * @param id The unique identifier (UUID) of the song to be deleted.
     * @return A {@link ResponseEntity} with a success message if deletion is successful.
     *         Returns a 403 FORBIDDEN status if the user is not authorized,
     *         or a 500 INTERNAL_SERVER_ERROR status if an unexpected error occurs.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSong(@PathVariable("id") UUID id) {
        try {
            songService.deleteSong(id);
            return ResponseEntity.ok("Song and related files successfully deleted.");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while deleting the song: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Song could not be deleted: " + e.getMessage());
        }
    }
}

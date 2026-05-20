package com.example.soundboot.controller;

import com.example.soundboot.dto.request.CreatePlaylistRequest;
import com.example.soundboot.dto.response.ListPlaylistResponse;
import com.example.soundboot.service.playlistService.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller class for managing playlist-related operations.
 * Provides endpoints for creating, deleting, and listing playlists.
 */
@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
@Slf4j
public class PlaylistController {

    private final PlaylistService playlistService;

    /**
     * Endpoint to create a new playlist.
     *
     * @param request A {@link CreatePlaylistRequest} containing the details of the playlist to be created.
     * @return A {@link ResponseEntity} with a success message if the playlist is created,
     *         or an error message with a 500 INTERNAL_SERVER_ERROR status if creation fails.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createPlaylist(@RequestBody CreatePlaylistRequest request) {
        try {
            playlistService.createPlaylist(request);
            return ResponseEntity.ok("Playlist successfully created.");
        } catch (Exception e) {
            log.error("An error occurred while creating the playlist: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Playlist could not be created: " + e.getMessage());
        }
    }

    /**
     * Endpoint to delete an existing playlist by its unique identifier.
     * Only the creator of the playlist is authorized to delete it.
     *
     * @param id The unique identifier (UUID) of the playlist to be deleted.
     * @return A {@link ResponseEntity} with a success message if the deletion is successful.
     *         Returns a 403 FORBIDDEN status if the user is not authorized,
     *         or a 500 INTERNAL_SERVER_ERROR status if an unexpected error occurs.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlaylist(@PathVariable("id") UUID id) {
        try {
            playlistService.deletePlaylist(id);
            return ResponseEntity.ok("Playlist successfully deleted.");
        } catch (AccessDeniedException e) {
            // Catch authorization exception and return 403 Forbidden
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while deleting the playlist: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Playlist could not be deleted: " + e.getMessage());
        }
    }

    /**
     * Endpoint to retrieve the list of songs in a specific playlist.
     *
     * @param id The unique identifier (UUID) of the playlist.
     * @return A {@link ResponseEntity} containing a string representation of the list of songs
     *         ({@link ListPlaylistResponse}) if successful. Returns a 403 FORBIDDEN status if access is denied,
     *         or a 500 INTERNAL_SERVER_ERROR status if an unexpected error occurs.
     */
    @GetMapping("/{id}")
    public ResponseEntity<String> listPlaylist(@PathVariable("id") UUID id) {
        try {
            List<ListPlaylistResponse> songs = playlistService.listPlaylist(id);
            return ResponseEntity.ok(songs.toString());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            log.error("An error occurred while listing the songs of the playlist: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Songs could not be listed: " + e.getMessage());
        }
    }
}

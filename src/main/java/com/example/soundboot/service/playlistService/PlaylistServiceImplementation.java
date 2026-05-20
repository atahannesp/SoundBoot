package com.example.soundboot.service.playlistService;


import com.example.soundboot.dto.request.CreatePlaylistRequest;
import com.example.soundboot.dto.response.ListPlaylistResponse;
import com.example.soundboot.entity.PlaylistEntity;
import com.example.soundboot.entity.SongEntity;
import com.example.soundboot.entity.UserEntity;
import com.example.soundboot.repository.PlaylistRepository;
import com.example.soundboot.repository.SongRepository;
import com.example.soundboot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the PlaylistService interface.
 * Handles the business logic for creating, deleting, and listing playlists.
 */
@Service
@RequiredArgsConstructor
public class PlaylistServiceImplementation implements PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new playlist based on the provided request details.
     * The method retrieves the currently authenticated user and the requested songs.
     * Throws exceptions if any songs or the user cannot be found.
     *
     * @param request The data transfer object containing the playlist name, public status, and list of song IDs.
     * @throws RuntimeException if any of the requested songs cannot be found in the database.
     * @throws RuntimeException if the authenticated user cannot be found in the database.
     */
    @Override
    public void createPlaylist(CreatePlaylistRequest request) {

        // Convert UUIDs to SongEntity objects
        List<SongEntity> songs = (List<SongEntity>) songRepository
                .findAllById(request.getSongs());

        if (songs.size() != request.getSongs().size()) {
            throw new RuntimeException("Some songs could not be found.");
        }

        // Retrieve the username from the current authentication token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;
        String currentUsername = authentication.getName();

        Optional<UserEntity> user = userRepository.findByUsername(currentUsername);
        if(user.isPresent()) {
            PlaylistEntity playlist = PlaylistEntity.builder()
                    .name(request.getName())
                    .isPublic(request.isPublic())
                    .user(user.get())
                    .songs(songs)
                    .build();
            songs.forEach(song -> song.setPlaylist(playlist));
            playlistRepository.save(playlist);
        } else {
            throw new RuntimeException("User not found.");
        }
    }

    /**
     * Deletes an existing playlist by its unique identifier.
     * Only the user who created the playlist is authorized to delete it.
     *
     * @param id The unique identifier (UUID) of the playlist to be deleted.
     * @throws RuntimeException if the playlist cannot be found.
     * @throws AccessDeniedException if the authenticated user is not the owner of the playlist.
     */
    @Override
    public void deletePlaylist(UUID id) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assert authentication != null;

        String currentUsername = authentication.getName();

        PlaylistEntity playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found."));

        if (!playlist.getUser().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You do not have permission to delete this playlist.");
        }

        playlistRepository.delete(playlist);
    }

    /**
     * Retrieves a list of songs belonging to a specific playlist.
     * Maps the internal song entities to response DTOs.
     *
     * @param id The unique identifier (UUID) of the playlist.
     * @return A list of {@link ListPlaylistResponse} objects representing the songs in the playlist.
     * @throws RuntimeException if the playlist cannot be found.
     */
    @Override
    public List<ListPlaylistResponse> listPlaylist(UUID id) {
        PlaylistEntity playlist = playlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Playlist not found."));

        return playlist.getSongs().stream()
                .map(song -> ListPlaylistResponse.builder()
                        .id(song.getId())
                        .title(song.getTitle())
                        .signedURL(song.getStorageKey())
                        .owner(song.getArtist())
                        .views((int) song.getPlayCount())
                        .build())
                .toList();
    }
}

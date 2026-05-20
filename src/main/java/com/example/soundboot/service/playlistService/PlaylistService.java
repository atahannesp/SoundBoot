package com.example.soundboot.service.playlistService;

import com.example.soundboot.dto.request.CreatePlaylistRequest;
import com.example.soundboot.dto.response.ListPlaylistResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface PlaylistService {
    
    void createPlaylist(CreatePlaylistRequest request);

    void deletePlaylist(UUID id);

    List<ListPlaylistResponse> listPlaylist(UUID id);
}

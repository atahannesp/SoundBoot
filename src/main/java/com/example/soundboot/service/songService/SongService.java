package com.example.soundboot.service.songService;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface SongService {
    void uploadSong(MultipartFile file, String title) throws Exception;
    void deleteSong(UUID id) throws Exception;
}

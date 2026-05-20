package com.example.soundboot.dto.request;

import com.example.soundboot.entity.enums.Genre;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
@Data
@NoArgsConstructor
public class CreateSongRequest {
    private String title;

    private String description;

    private String artist;

    private Genre genre;

    private String storageKey;

    private boolean isPublic;

    private UUID userId;

    MultipartFile file;
    }

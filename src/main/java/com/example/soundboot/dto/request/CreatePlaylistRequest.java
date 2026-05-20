package com.example.soundboot.dto.request;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CreatePlaylistRequest {
    String username;
    List<UUID> songs;
    boolean isPublic;
    String name;
}

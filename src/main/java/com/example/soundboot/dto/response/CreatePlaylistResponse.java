package com.example.soundboot.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CreatePlaylistResponse {

    private UUID userId;

    private List<UUID> songs;

    private String message;

}

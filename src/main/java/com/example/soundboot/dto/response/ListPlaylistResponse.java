package com.example.soundboot.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ListPlaylistResponse {
    private String title;
    private String signedURL;
    private UUID id;
    private String owner;
    private Integer views;
}

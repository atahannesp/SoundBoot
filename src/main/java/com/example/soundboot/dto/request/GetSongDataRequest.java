package com.example.soundboot.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class GetSongDataRequest {
    UUID id;
}
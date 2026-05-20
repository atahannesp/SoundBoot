package com.example.soundboot.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateSongResponse {
    String title;
    String signedURL;
}

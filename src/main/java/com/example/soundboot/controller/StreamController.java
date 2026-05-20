package com.example.soundboot.controller;

import com.example.soundboot.service.streamService.StreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for handling audio stream requests.
 * Provides endpoints to fetch HLS (.m3u8) playlists and segment (.ts) files.
 */
@RestController
@RequestMapping("/api/stream")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    /**
     * Endpoint for streaming song files via HTTP Live Streaming (HLS).
     * Serves both .m3u8 playlist files and .ts segment files based on the requested file name.
     *
     * @param trackId  The unique identifier of the track to be streamed.
     * @param fileName The name of the requested HLS file (either index.m3u8 or a segment file like seg000.ts).
     * @return A {@link ResponseEntity} containing the requested {@link Resource} with the appropriate content type,
     *         or a 404 NOT_FOUND status if the file cannot be read or does not exist.
     */
    @GetMapping("/{trackId}/{fileName}")
    public ResponseEntity<?> streamSong(
            @PathVariable("trackId") String trackId,
            @PathVariable("fileName") String fileName) {

        try {
            Resource resource = streamService.getHlsFile(trackId, fileName);

            String contentType = "application/octet-stream";
            if (fileName.endsWith(".m3u8")) {
                contentType = "application/vnd.apple.mpegurl";
            } else if (fileName.endsWith(".ts")) {
                contentType = "video/MP2T";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}

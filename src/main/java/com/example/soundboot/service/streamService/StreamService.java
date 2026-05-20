package com.example.soundboot.service.streamService;

import org.springframework.core.io.Resource;

public interface StreamService {
    Resource getHlsFile(String trackId, String fileName);
}

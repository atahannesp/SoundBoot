# SoundBoot - RESTful Audio Streaming API

SoundBoot is a Spring Boot-based backend application that enables users to upload audio tracks, manage playlists, and stream music using **HTTP Live Streaming (HLS)**.

Uploaded audio files are processed and segmented into `.m3u8` and `.ts` chunks using **FFmpeg** in the background. This allows client applications to stream audio progressively based on the user's playback position instead of downloading the entire file at once.

---

## Features

- **HLS Audio Streaming:** Automatically splits uploaded audio files (e.g., MP3) into 10-second segments using FFmpeg.
- **Play Count Tracking:** Increments the track's play count automatically whenever the HLS manifest file (`.m3u8`) is requested via the streaming endpoint.
- **JWT Authentication:** Secure, stateless user registration and authentication handled via JSON Web Tokens (JWT).
- **Playlist Management:** Users can create, view, and delete playlists. Access control ensures that playlists can only be deleted by their respective creators.
- **Relational Data Model:** Optimized data relations between Users, Songs, and Playlists using JPA/Hibernate with lazy fetching strategy.

---

## Tech Stack

- **Java 21 (LTS)**
- **Spring Boot 3.x**
- **Spring Security & JWT**
- **Spring Data JPA / Hibernate**
- **PostgreSQL**
- **FFmpeg** (For audio segmentation)
- **Docker & Docker Compose**
- **Lombok**

---

## Getting Started

### Prerequisites
Make sure you have the following installed on your local environment:
- **Docker** and **Docker Compose**

---

## Deployment via Docker (Recommended)

The project is containerized using a multi-stage Docker build to optimize container sizes and simplify dependencies like FFmpeg and PostgreSQL.

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/YOUR_USERNAME/SoundBoot.git](https://github.com/YOUR_USERNAME/SoundBoot.git)
   cd SoundBoot
   ```

2. **Run the application:**
   Launch the application along with the PostgreSQL database using Docker Compose:
   ```bash
   docker-compose up -d --build
   ```

The application will be accessible at `http://localhost:8080`.

---

## Local Development (Without Docker)

If you prefer to run the application natively, ensure **Java 21**, **PostgreSQL**, and **FFmpeg** are installed on your machine and configured in your system's `PATH`.

1. **Configure Database:**
   Create an empty database named `musicapp` (or your preferred name) in PostgreSQL.

2. **Update Configuration:**
   Modify `src/main/resources/application.properties` with your local credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/musicapp
   spring.datasource.username=your_postgres_user
   spring.datasource.password=your_postgres_password

   spring.servlet.multipart.max-file-size=50MB
   spring.servlet.multipart.max-request-size=50MB
   ```

3. **Build and Run:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

---

## API Endpoints

### Authentication
- `POST /api/v1/auth/signup` - Registers a new user. Accepts `RegisterRequest`.
- `POST /api/v1/auth/signin` - Authenticates a user and returns a JWT token. Accepts `LoginRequest`.

### Songs
- `POST /api/songs/upload` - Uploads an audio file and triggers HLS segmentation. Requires Bearer Token. (Form-Data: `file` and `title`).
- `DELETE /api/songs/{id}` - Deletes a song from both the database and storage. Requires Bearer Token.

### Playlists
- `POST /api/playlists/create` - Creates a new playlist. Accepts `CreatePlaylistRequest`.
- `GET /api/playlists/{id}` - Retrieves details and track lists within a playlist. Returns `List<ListPlaylistResponse>`.
- `DELETE /api/playlists/{id}` - Deletes a playlist. Authorized for the playlist creator only.

### Streaming
- `GET /api/stream/{trackId}/{fileName}` - Serves the dynamic HLS stream files (`index.m3u8` or `.ts` segments).

---

## How the HLS Pipeline Works

1. **Upload:** A user uploads an audio file via the `/api/songs/upload` endpoint.
2. **Segmentation:** `AudioSegmentationService` invokes **FFmpeg** via Java's `ProcessBuilder`. The original file is sliced into segments and saved inside the `uploads/hls/{trackId}/` directory.
3. **Manifest Request:** When a client-side media player requests the `index.m3u8` file, the `StreamService` updates the database to increment the track's overall `play_count`.
4. **Playback:** The player reads the manifest file and continuously fetches the consecutive `.ts` data blocks seamlessly.

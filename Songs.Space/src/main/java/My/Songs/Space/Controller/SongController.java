package My.Songs.Space.Controller;

import My.Songs.Space.Dto.SongDto;
import My.Songs.Space.Dto.SongUpdateDTO;
import My.Songs.Space.Entity.Song;
import My.Songs.Space.Entity.UserInfo;
import My.Songs.Space.Repository.SongRepository;
import My.Songs.Space.Service.SongService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;

import java.util.List;


@RestController
//@RequestMapping("/songs")
public class SongController {

    @Autowired
    SongService songService;
    @Autowired

    SongRepository songRepository;
    private Date uploadedDate;

    @PreAuthorize("hasAuthority('listener')")

        @GetMapping("/allsongs")
        public ResponseEntity<List<Song>> getAllSongs() {
            List<Song> songs = songService.getAllSongs();
            return new ResponseEntity<>(songs, HttpStatus.OK);

        }


    @PreAuthorize("hasAuthority('listener')")
    @GetMapping("/songs")
    public ResponseEntity<?> getSongs(
            @RequestParam(required = false) String searchTitle,
            @RequestParam(required = false) String filterArtist,
            @RequestParam(required = false) String filterGenres,
//            @RequestParam(value = "title", defaultValue = "") String title,
            @RequestParam(required = false) String title,
//            @RequestParam(required = false, defaultValue = "") String date) {
            @RequestParam(required = false) String date){

        // Check if at least one search, filter, or sort parameter is provided
        if (StringUtils.isAllBlank(searchTitle, filterArtist, filterGenres) && StringUtils.isBlank(title) && StringUtils.isBlank(date)) {
            String errorMessage = "No search criteria provided. Please provide at least one valid search, filter, or sort parameter.";
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }

        if (StringUtils.isNotBlank(date)) {
            try {
                OffsetDateTime targetDateTime = OffsetDateTime.parse(date); // Parse the provided date string to OffsetDateTime
                List<SongDto> songsByDate = songService.getSongsSortedByUploadedDate(targetDateTime);
                return new ResponseEntity<>(songsByDate, HttpStatus.OK);
            } catch (DateTimeParseException e) {
                String errorMessage = "Invalid date format. Please provide a valid date in ISO-8601 format (e.g., 2023-07-31T12:00:00Z).";
                return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
            } catch (IllegalArgumentException e) {
                String errorMessage = e.getMessage();
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            // If sortField is provided, sort by title matching the specified value
            List<SongDto> songsByTitle;
            try {
                songsByTitle = songService.getSongsSortedByTitle(searchTitle, filterArtist, filterGenres, title);
            } catch (IllegalArgumentException e) {
                String errorMessage = e.getMessage();
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(songsByTitle, HttpStatus.OK);
        }
    }



        // sample posting for  storing data in databse
//    @PostMapping("/post")
//    public ResponseEntity<Song> addSong(
//            @RequestParam("thumbnail") MultipartFile thumbnailFile,
//            @RequestParam("title") String title,
//            @RequestParam("genres") String genres,
//            @RequestParam("uploadedDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date uploadedDate,
//            @RequestParam("artist") String artist
//    ) {
//        try {
//            Song song = new Song();
//            song.setTitle(title);
//            song.setGenres(genres);
//            song.setUploadedDate(uploadedDate);
//            song.setArtist(artist);
//            song.setThumbnail(thumbnailFile.getBytes());
//
//            Song savedSong = songService.saveSong(song);
//            return new ResponseEntity<>(savedSong, HttpStatus.CREATED);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }


        // add users and password to db
//    @PostMapping("/add")
//    public String addNewUser(@RequestBody UserInfo userInfo) {
//        return songService.addUser(userInfo);
//    }


    @PreAuthorize("hasAuthority('artist')")
    @DeleteMapping("/{songId}")
    public ResponseEntity<String> deleteSong(@PathVariable Long songId, Authentication authentication) {
//        String artistUsername = principal.getName();

        String artistUsername = authentication.getName();
        // Retrieve the song by ID
        Song songToDelete = songRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("Song not found"));

        // Check if the authenticated artist is the owner of the song
        if (songToDelete.getArtist().equals(artistUsername)) {
            // Delete the song
            songRepository.delete(songToDelete);
            return ResponseEntity.ok("Song deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this song");
        }
    }





    @PreAuthorize("hasAuthority('artist')")
    @PostMapping("/upload")
    public ResponseEntity<String> addSong(@RequestParam(value = "title", required = false) String title,
                                          @RequestParam(value = "genres", required = false) String genres,
                                          @RequestParam(value = "uploadedDate", required = false) String uploadedDateStr,
                                          @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnailFile,
                                          @RequestParam(value = "artist", required = true) String artist,
                                          Principal principal) {
        String authenticatedArtist = principal.getName();

        // Check if the authenticated artist matches the provided artist name
        if (artist != null && !authenticatedArtist.equals(artist)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to upload songs for other artists.");
        }

        // Create a new Song object with the extracted data
        Song newSong = new Song();

        if (title != null) {
            newSong.setTitle(title);
        }

        if (genres != null) {
            newSong.setGenres(genres);
        }

        if (uploadedDateStr != null) {
            Date uploadedDate = parseUploadedDate(uploadedDateStr);
            newSong.setUploadedDate(uploadedDate);
        }

        if (artist != null) {
            newSong.setArtist(artist);
        }

        try {
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                // Convert the MultipartFile to a byte array and set it as the thumbnail
                newSong.setThumbnail(thumbnailFile.getBytes());
            }

            // Save the new song to the database
            songRepository.save(newSong);

            return ResponseEntity.ok("Song added successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading thumbnail");
        }
    }

    private Date parseUploadedDate(String uploadedDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(uploadedDateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format for uploadedDate: " + uploadedDateStr);
        }
    }


    @PreAuthorize("hasAuthority('artist')")
    @PutMapping("/{songId}")
    public ResponseEntity<String> updateSong(@PathVariable Long songId,
                                             @ModelAttribute SongUpdateDTO songUpdateDTO,
//                                             @RequestBody SongUpdateDTO songUpdateDTO,
                                             Authentication authentication) throws IOException {
        // 1: Check if the user is authenticated (is artist or not)
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to update a song.");
        }

        // 2: Get the authenticated artist's username
        String authenticatedArtist = authentication.getName();

        // Getting the Song which is already present in the db using the songId
        Optional<Song> optionalSong = songRepository.findById(songId);

        if (optionalSong.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Song not found.");
        }

        Song existingSong = optionalSong.get();

        // 3: Checking if the authenticated artist matches the provided artist name
        if (!authenticatedArtist.equals(existingSong.getArtist())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update songs for other artists.");
        }

        // Proceed with the update as the artist is authorized to modify the song

        // Update the title and genres if provided in the DTO
        if (songUpdateDTO.getTitle() != null) {
            existingSong.setTitle(songUpdateDTO.getTitle());
        }

        if (songUpdateDTO.getGenres() != null) {
            existingSong.setGenres(songUpdateDTO.getGenres());
        }

        // Update the uploaded date if provided in the DTO
        if (songUpdateDTO.getUploadedDate() != null) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date uploadedDate = format.parse(songUpdateDTO.getUploadedDate());
                existingSong.setUploadedDate(uploadedDate);
            } catch (ParseException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format. Use 'yyyy-MM-dd'.");
            }
        }

        // Update the thumbnail if provided in the DTO
        if (songUpdateDTO.getThumbnailFile() != null) {
            existingSong.setThumbnail(songUpdateDTO.getThumbnailFile().getBytes());
        }

        // Save the updated song to the db
        songRepository.save(existingSong);
        System.out.println("Received update request for songId: " + songId);
        System.out.println("Title: " + songUpdateDTO.getTitle());
        System.out.println("Genres: " + songUpdateDTO.getGenres());
        System.out.println("Uploaded Date: " + songUpdateDTO.getUploadedDate());
        System.out.println("Thumbnail File: " + songUpdateDTO.getThumbnailFile());
        return ResponseEntity.ok("Song updated successfully");
    }

}
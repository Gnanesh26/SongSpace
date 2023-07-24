package My.Songs.Space.Controller;

import My.Songs.Space.Dto.SongDto;
import My.Songs.Space.Dto.SongUpdateDTO;
import My.Songs.Space.Entity.Song;
import My.Songs.Space.Entity.UserInfo;
import My.Songs.Space.Repository.SongRepository;
import My.Songs.Space.Service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import java.util.List;

@RestController
//@RequestMapping("/songs")
public class SongController {

    @Autowired
    SongService songService;
    @Autowired

    SongRepository songRepository;
    private Date uploadedDate;

//    @PreAuthorize("hasAuthority('listener')")
//    @GetMapping("/allsongs")
//    public ResponseEntity<List<Song>> getAllSongs() {
//        List<Song> songs = songService.getAllSongs();
//        return new ResponseEntity<>(songs, HttpStatus.OK);
//

        @PreAuthorize("hasAuthority('listener')")
////


        @GetMapping("/songs")
        public ResponseEntity<?> getSongs(
                @RequestParam(required = false) String searchTitle,
                @RequestParam(required = false) String filterArtist,
                @RequestParam(required = false) String filterGenres,
                @RequestParam(value = "sortField", defaultValue = "title") String sortField
//                @RequestParam(required = false) String sortField
        ) {

                // Check if all filtering criteria are null or empty
                if (searchTitle == null && filterArtist == null && filterGenres == null) {
                    String infoMessage = "Please provide information about songs.";
                    return new ResponseEntity<>(infoMessage, HttpStatus.BAD_REQUEST);
                }

                try {
                List<Song> songs = songService.getSongs(searchTitle, filterArtist, filterGenres);

                // without the thumbnail field
                List<SongDto> simplifiedSongs = songs.stream()
                        .map(song -> new SongDto(song.getTitle(), song.getGenres(), song.getUploadedDate(), song.getArtist()))
                        .collect(Collectors.toList());

                // Split(list)  one for songs starting with the given letter and another for the rest
                List<SongDto> startingWithLetter = new ArrayList<>();
                List<SongDto> remainingSongs = new ArrayList<>();
                simplifiedSongs.forEach(song -> {
                    if (song.getTitle().toLowerCase().startsWith(sortField.toLowerCase())) {
                        startingWithLetter.add(song);
                    } else {
                        remainingSongs.add(song);
                    }
                });

                // Sort each list individually
                startingWithLetter.sort(Comparator.comparing(SongDto::getTitle));
                remainingSongs.sort(Comparator.comparing(SongDto::getTitle));

                // Add  the two lists
                List<SongDto> sortedSongs = new ArrayList<>(startingWithLetter);
                sortedSongs.addAll(remainingSongs);
                simplifiedSongs.sort(Comparator.comparing(SongDto::getUploadedDate).reversed());
                return new ResponseEntity<>(sortedSongs, HttpStatus.OK);
            } catch (IllegalArgumentException e) {
                // no songs are found with required fields
                String errorMessage = "No songs found";
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        }


    //
    @PreAuthorize("hasAuthority('listener')")
    @GetMapping("/sortbydate")
    public ResponseEntity<List<SongDto>> getSongs(@RequestParam("date") String dateStr) {
        OffsetDateTime targetDateTime = OffsetDateTime.parse(dateStr); // Parse the provided date string to OffsetDateTime

        List<SongDto> songs = songService.getSongsSortedByUploadedDate(targetDateTime);
        return new ResponseEntity<>(songs, HttpStatus.OK);
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
    public ResponseEntity<String> deleteSong(@PathVariable Long songId, Principal principal) {
        String artistUsername = principal.getName();

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


    private Date parseUploadedDate(String uploadedDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(uploadedDateStr);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format for uploadedDate: " + uploadedDateStr);
        }
    }


    @PreAuthorize("hasAuthority('artist')")
    @PostMapping("/upload")
    public ResponseEntity<String> addSong(@RequestParam("title") String title,
                                          @RequestParam("genres") String genres,
                                          @RequestParam("uploadedDate") String uploadedDateStr,
                                          @RequestParam("thumbnail") MultipartFile thumbnailFile,
                                          @RequestParam("artist") String artist,
                                          Principal principal) {
        String authenticatedArtist = principal.getName();

        // Check if the authenticated artist matches the provided artist name
        if (!authenticatedArtist.equals(artist)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to upload songs for other artists.");
        }

        // Create a new Song object with the extracted data
        Song newSong = new Song();
        newSong.setTitle(title);
        newSong.setGenres(genres);

        // to handle date parsing appropriately
        Date uploadedDate = parseUploadedDate(uploadedDateStr);
        newSong.setUploadedDate(uploadedDate);

        // Set the artist of the new song based by the given artist name
        newSong.setArtist(artist);

        try {
            // Convert the MultipartFile to a byte array and set it as the thumbnail
            newSong.setThumbnail(thumbnailFile.getBytes());

            // Save the new song to the database
            songRepository.save(newSong);

            return ResponseEntity.ok("Song added successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading thumbnail");
        }
    }



    @PreAuthorize("hasAuthority('artist')")
    @PutMapping("/{songId}")
    public ResponseEntity<String> updateSong(@PathVariable Long songId,
                                             @ModelAttribute SongUpdateDTO songUpdateDTO,
                                             Principal principal) {
        String authenticatedArtist = principal.getName();

        // Checking if the authenticated artist matches the provided artist name
        if (!authenticatedArtist.equals(songUpdateDTO.getArtist())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update songs for other artists.");
        }

        // Retrieve the  Song  which  already present  in db using the songId
        Optional<Song> optionalSong = songRepository.findById(songId);

        if (optionalSong.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Song not found.");
        }

        Song existingSong = optionalSong.get();

        if (songUpdateDTO.getTitle() != null) {
            existingSong.setTitle(songUpdateDTO.getTitle());
        }

        if (songUpdateDTO.getGenres() != null) {
            existingSong.setGenres(songUpdateDTO.getGenres());
        }

        if (songUpdateDTO.getUploadedDate() != null) {
            Date uploadedDate = parseUploadedDate(songUpdateDTO.getUploadedDate());
            existingSong.setUploadedDate(uploadedDate);
        }

        try {
            // Convert the MultipartFile to a byte array and set it as the new thumbnail(pic)
            if (songUpdateDTO.getThumbnailFile() != null) {
                existingSong.setThumbnail(songUpdateDTO.getThumbnailFile().getBytes());
            }

            // Save the updated song to the db
            songRepository.save(existingSong);

            return ResponseEntity.ok("Song updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading thumbnail");
        }
    }
}


package My.Songs.Space.Service;

import My.Songs.Space.Dto.SongDto;
import My.Songs.Space.Entity.Song;
import My.Songs.Space.Entity.UserInfo;
import My.Songs.Space.Repository.SongRepository;
import My.Songs.Space.Repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SongService {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    SongRepository songRepository;
    @Autowired
    UserInfoRepository userInfoRepository;

    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    public Song saveSong(Song song) {
        return songRepository.save(song);
    }

//    public List<Song> getSongs(String searchTitle, String filterArtist, String filterGenres) {
//        List<Song> filteredSongs = songRepository.findAll().stream()
//                .filter(song ->
//                        (searchTitle == null || song.getTitle().contains(searchTitle)) &&
//                                (filterArtist == null || song.getArtist().equals(filterArtist)) &&
//                                (filterGenres == null || song.getGenres().equals(filterGenres)))
//                .collect(Collectors.toList());
//
//        return filteredSongs;
//    }
//public List<Song> getSongs(String searchTitle, String filterArtist, String filterGenres) {
//    if (searchTitle == null && filterArtist == null && filterGenres == null) {
//        // If all parameters are null, return a message indicating to provide search criteria
//        throw new IllegalArgumentException("Please provide search criteria (searchTitle, filterArtist, or filterGenres).");
//    }
//
//    List<Song> filteredSongs = songRepository.findAll().stream()
//            .filter(song ->
//                    (searchTitle == null || song.getTitle().contains(searchTitle)) &&
//                            (filterArtist == null || song.getArtist().equals(filterArtist)) &&
//                            (filterGenres == null || song.getGenres().equals(filterGenres)))
//            .collect(Collectors.toList());
//
//    return filteredSongs;
//}
//public List<Song> getSongs(String searchTitle, String filterArtist, String filterGenres) {
//    List<Song> filteredSongs = songRepository.findAll().stream()
//            .filter(song ->
//                    (searchTitle == null || song.getTitle().contains(searchTitle)) &&
//                            (filterArtist == null || song.getArtist().equals(filterArtist)) &&
//                            (filterGenres == null || song.getGenres().equals(filterGenres)))
//            .collect(Collectors.toList());
//
//    if (filteredSongs.isEmpty()) {
//        throw new IllegalArgumentException("No songs found with the provided criteria.");
//    }
//
//    return filteredSongs;
//}


    public String addUser(UserInfo userInfo) {
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        userInfoRepository.save(userInfo);
        return "user added to system ";
    }

    public List<Song> getSongs(String searchTitle, String filterArtist, String filterGenres) {
        List<Song> filteredSongs = songRepository.findAll().stream()
                .filter(song ->
                        (searchTitle == null || song.getTitle().contains(searchTitle)) &&
                                (filterArtist == null || song.getArtist().equals(filterArtist)) &&
                                (filterGenres == null || song.getGenres().equals(filterGenres)))
                .collect(Collectors.toList());

        if (filteredSongs.isEmpty()) {
            throw new IllegalArgumentException("No songs found with the provided criteria.");
        }

        return filteredSongs;
    }


    public List<SongDto> getSongsSortedByUploadedDate(OffsetDateTime targetDateTime) {
        List<Song> songs = songRepository.findAll();

        // Separate songs with the provided date and others
        List<Song> givenDateSongs = new ArrayList<>();
        List<Song> remainingSongs = new ArrayList<>();

        for (Song song : songs) {
            OffsetDateTime songDateTime = song.getUploadedDate().toInstant().atOffset(ZoneOffset.UTC);
            if (songDateTime.isEqual(targetDateTime)) {
                givenDateSongs.add(song);
            } else {
                remainingSongs.add(song);
            }
        }

        // Sort the remaining songs in ascending order of uploaded dates
        remainingSongs.sort(Comparator.comparing(song -> song.getUploadedDate().toInstant().atOffset(ZoneOffset.UTC)));

        // Combine both lists and convert to SongDto objects
        List<SongDto> songDtos = Stream.concat(
                givenDateSongs.stream().map(song -> new SongDto(song.getTitle(), song.getGenres(), song.getUploadedDate(), song.getArtist())),
                remainingSongs.stream().map(song -> new SongDto(song.getTitle(), song.getGenres(), song.getUploadedDate(), song.getArtist()))
        ).collect(Collectors.toList());

        return songDtos;
    }

//
//    public List<SongDto> getSongsSortedByUploadedDate(LocalDate targetDate) {
//        List<Song> songs = songRepository.findAll(Sort.by(Sort.Direction.ASC, "uploadedDate"));
//
//        // Convert to SongDto objects
//        List<SongDto> songDtos = songs.stream()
//                .map(song -> new SongDto(song.getTitle(), song.getGenres(), song.getUploadedDate(), song.getArtist()))
//                .collect(Collectors.toList());
//
//        return songDtos;
//    }
}



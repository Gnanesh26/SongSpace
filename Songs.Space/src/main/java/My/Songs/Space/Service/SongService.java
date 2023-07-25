package My.Songs.Space.Service;

import My.Songs.Space.Dto.SongDto;
import My.Songs.Space.Entity.Song;
import My.Songs.Space.Entity.UserInfo;
import My.Songs.Space.Repository.SongRepository;
import My.Songs.Space.Repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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



    public String addUser(UserInfo userInfo) {
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        userInfoRepository.save(userInfo);
        return "user added to system ";
    }


    public List<SongDto> getSongsSortedByTitle(String searchTitle, String filterArtist, String filterGenres, String sortField) {
        List<Song> songs = songRepository.findAll();

        // Filter songs based on searchTitle, filterArtist, and filterGenres
        List<Song> filteredSongs = songs.stream()
                .filter(song ->
                        (searchTitle == null || song.getTitle().toLowerCase().contains(searchTitle.toLowerCase())) &&
                                (filterArtist == null || song.getArtist().equalsIgnoreCase(filterArtist)) &&
                                (filterGenres == null || song.getGenres().equalsIgnoreCase(filterGenres)))
                .collect(Collectors.toList());

        List<SongDto> sortedSongs;

        if (sortField != null && !sortField.isEmpty()) {
            // Check if any songs match the specified title in sortField
            List<Song> songsMatchingSortField = filteredSongs.stream()
                    .filter(song -> song.getTitle().equalsIgnoreCase(sortField))
                    .collect(Collectors.toList());

            if (songsMatchingSortField.isEmpty()) {
                throw new IllegalArgumentException("No songs found with the provided title.");
            }

            // Sort by title matching the specified value
            List<SongDto> songsWithTitle = filteredSongs.stream()
                    .filter(song -> song.getTitle().equalsIgnoreCase(sortField))
                    .map(song -> new SongDto(song.getTitle(), song.getGenres(), song.getUploadedDate(), song.getArtist()))
                    .collect(Collectors.toList());

            List<SongDto> remainingSongs = filteredSongs.stream()
                    .filter(song -> !song.getTitle().equalsIgnoreCase(sortField))
                    .map(song -> new SongDto(song.getTitle(), song.getGenres(), song.getUploadedDate(), song.getArtist()))
                    .collect(Collectors.toList());

            songsWithTitle.sort(Comparator.comparing(SongDto::getTitle));
            remainingSongs.sort(Comparator.comparing(SongDto::getTitle));

            // Combine the sorted lists
            songsWithTitle.addAll(remainingSongs);

            sortedSongs = songsWithTitle;
        } else {
            // If sortField is empty, sort by uploaded date in descending order
            sortedSongs = filteredSongs.stream()
                    .map(song -> new SongDto(song.getTitle(), song.getGenres(), song.getUploadedDate(), song.getArtist()))
                    .sorted(Comparator.comparing(SongDto::getUploadedDate).reversed())
                    .collect(Collectors.toList());

            if (sortedSongs.isEmpty()) {
                throw new IllegalArgumentException("No songs found with the provided search criteria.");
            }
        }

        return sortedSongs;
    }

    public List<SongDto> getSongsSortedByUploadedDate(OffsetDateTime targetDateTime) {
        List<Song> songs = songRepository.findAll();

        // Separate songs with the provided date
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

        if (givenDateSongs.isEmpty()) {
            throw new IllegalArgumentException("No songs found");
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
}
package My.Songs.Space.Service;

import My.Songs.Space.Entity.Song;
import My.Songs.Space.Entity.UserInfo;
import My.Songs.Space.Repository.SongRepository;
import My.Songs.Space.Repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

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


}



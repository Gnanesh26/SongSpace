package My.Songs.Space.Entity;

import jakarta.persistence.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "songs")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String genres;

    @Column
    private Date uploadedDate;

    @Lob
    @Column(length = 1048576)
    private byte[] thumbnail;


    @Column
    private String artist;

    @Transient
    byte[] thumbnailData;

    public Song() {
    }

    public Song(String title, String genres, Date uploadedDate, byte[] thumbnail, String artist) {
    }

    public void setThumbnailData(  byte[] thumbnailData){
         this.thumbnailData=thumbnailData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public Date getUploadedDate() {
        return uploadedDate;
    }

    public void setUploadedDate(Date uploadedDate) {
        this.uploadedDate = uploadedDate;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id) && Objects.equals(title, song.title) && Objects.equals(genres, song.genres) && Objects.equals(uploadedDate, song.uploadedDate) && Arrays.equals(thumbnail, song.thumbnail) && Objects.equals(artist, song.artist);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, title, genres, uploadedDate, artist);
        result = 31 * result + Arrays.hashCode(thumbnail);
        return result;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", genres='" + genres + '\'' +
                ", uploadedDate=" + uploadedDate +
                ", thumbnail=" + Arrays.toString(thumbnail) +
                ", artist='" + artist + '\'' +
                '}';
    }

    public void setThumbnailBase64(String thumbnailBase64) {
    }

    public void setThumbnailUrl(String thumbnailUrl) {
    }
}
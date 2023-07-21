package My.Songs.Space.Dto;

import java.util.Date;

public class SongDto {


    private String title;
    private String genres;
    private Date uploadedDate;
    private String artist;

    public SongDto() {
    }

    public SongDto( String title, String genres, Date uploadedDate, String artist) {

        this.title = title;
        this.genres = genres;
        this.uploadedDate = uploadedDate;
        this.artist = artist;
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

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
}
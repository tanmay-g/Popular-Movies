package tanmaygodbole.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class MovieData implements Parcelable{
    private String posterPath;
    private String title;
    private String overview;
    private double rating;
    private String releaseDate;

    public MovieData(String posterPath, String title, String overview, double rating, String releaseDate) {
        this.posterPath = posterPath;
        this.title = title;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    protected MovieData(Parcel in) {
        posterPath = in.readString();
        title = in.readString();
        overview = in.readString();
        rating = in.readDouble();
        releaseDate = in.readString();
    }

    public static final Creator<MovieData> CREATOR = new Creator<MovieData>() {
        @Override
        public MovieData createFromParcel(Parcel in) {
            return new MovieData(in);
        }

        @Override
        public MovieData[] newArray(int size) {
            return new MovieData[size];
        }
    };

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(posterPath);
        parcel.writeString(title);
        parcel.writeString(overview);
        parcel.writeDouble(rating);
        parcel.writeString(releaseDate);
    }
}

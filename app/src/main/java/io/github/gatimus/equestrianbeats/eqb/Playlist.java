package io.github.gatimus.equestrianbeats.eqb;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Playlist {

    public int id;
    public String title;
    @SerializedName("num_tracks")
    public int numTracks;
    public String description;
    @SerializedName("html_description")
    public String htmlDescription;
    public User author;
    public List<Track> tracks;
    public Uri link;

    @Override
    public String toString(){
        return title;
    }

}

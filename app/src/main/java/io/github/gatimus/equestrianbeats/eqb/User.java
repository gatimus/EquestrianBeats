package io.github.gatimus.equestrianbeats.eqb;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class User {

    public int id;
    public String name;
    public String avatar;
    public String description;
    @SerializedName("html_description")
    public String htmlDescription;
    public List<Track> tracks;
    public List<Playlist> playlists;
    @SerializedName("num_favorites")
    public int numFavorites;
    @SerializedName("num_followers")
    public int numFollowers;
    public String link;

    @Override
    public String toString(){
        return name;
    }

}

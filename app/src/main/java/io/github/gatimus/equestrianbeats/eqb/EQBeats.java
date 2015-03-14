package io.github.gatimus.equestrianbeats.eqb;

import java.util.List;

import io.github.gatimus.equestrianbeats.BuildConfig;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public class EQBeats {

    public static interface EQBInterface{

        //user calls
        @GET("/user/{id}/json")
        void getUser(@Path("id")int id, Callback<User> cb);
        @GET("/users/search/json")
        void searchUsers(@Query("q")String query, Callback<List<User>> cb);
        @GET("/user/{id}/favorites/json")
        void getUserFavorites(@Path("id")int id, Callback<List<Track>> cb);
        @GET("/artists/json")
        void getArtists(Callback<List<User>> cb);

        //track calls
        @GET("/track/{id}/json")
        void getTrack(@Path("id")int id, Callback<Track> cb);
        @GET("/tracks/search/json")
        void searchTracks(@Query("q")String query, Callback<List<Track>> cb);
        @GET("/tracks/search/exact/json")
        void searchExactTrack(@Query("artist")String artist, @Query("track")String track, Callback<List<Track>> cb);
        @GET("/tracks/latest/json")
        void getLatestTracks(Callback<List<Track>> cb);
        @GET("/tracks/featured/json")
        void getFeaturedTracks(Callback<List<Track>> cb);
        @GET("/tracks/random/json")
        void getRandomTracks(Callback<List<Track>> cb);
        @GET("/tracks/all/json")
        void getAllTracks(Callback<List<Track>> cb);
        @GET("/tracks/all/json")
        void getAllTracksPaged(@Query("per_page")int count, @Query("page")int page, Callback<List<Track>> cb);

        //playlist
        @GET("/playlist/{id}/json")
        void getPlaylist(@Path("id")int id, Callback<Playlist> cb);

    }

    public static EQBInterface getEQBInterface(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://eqbeats.org")
                .build();

        if(BuildConfig.DEBUG)
            restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);

        return restAdapter.create(EQBInterface.class);
    }

}

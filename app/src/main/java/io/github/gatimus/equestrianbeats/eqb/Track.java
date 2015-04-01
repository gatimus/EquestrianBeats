package io.github.gatimus.equestrianbeats.eqb;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Track {

    public int id;
    public String title;
    public String description;
    @SerializedName("html_description")
    public String htmlDescription;
    //public JsonElement[] tags;
    public String license;
    public User artist;
    public Uri link;
    public Map<String, Uri> download;
    public Map<String, Uri> stream;
    public Stats stats;
    public float timestamp;

    public class Stats {

        public Totals totals;
        @SerializedName("unique_totals")
        public Totals uniqueTotals;

        public class Totals {
            public int trackView;
            public int trackPlay;
            public int trackDownload;
        }

    }

    @Override
    public String toString(){
        return title;
    }

}

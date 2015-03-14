package io.github.gatimus.equestrianbeats.eqb;

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
    public String artist;
    public String link;
    public Map<String, String> download;
    public Map<String, String> stream;
    public Stats stats;
    public float timestamp;

    public class Stats {

        public Totals totals;
        @SerializedName("unique_totals")
        public UniqueTotals uniqueTotals;

        public class Totals {
            public int trackView;
            public int trackPlay;
            public int trackDownload;
        }

        public class UniqueTotals {
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

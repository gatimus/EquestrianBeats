package io.github.gatimus.equestrianbeats.eqb;

import android.net.Uri;
import android.util.Log;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

public class UriDeserializer implements JsonDeserializer<Uri> {
    @Override
    public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Uri uri = new Uri.Builder().build();
        String stringUri = json.getAsString();
        if(!stringUri.isEmpty()){
            try {
                uri = Uri.parse(stringUri);
            } catch (NullPointerException e) {
                Log.e(getClass().getSimpleName(), e.toString());
            }
        }
        return uri;
    }
}
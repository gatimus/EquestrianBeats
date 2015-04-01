package io.github.gatimus.equestrianbeats;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaDescription;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaSession;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.service.media.MediaBrowserService;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.gatimus.equestrianbeats.eqb.EQBeats;
import io.github.gatimus.equestrianbeats.eqb.Track;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MusicService extends MediaBrowserService implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

    public static final String MEDIA_ID_ROOT = "__ROOT__";
    public static final String MEDIA_ID_LATEST = "Latest";
    public static final String MEDIA_ID_FEATURED = "Featured";
    public static final String MEDIA_ID_RANDOM = "Random";

    public static final String ACTION_PLAY = "io.github.gatimus.equestrianbeats.action.play";
    public static final String ACTION_PAUSE = "io.github.gatimus.equestrianbeats.action.pause";
    public static final String ACTION_STOP = "io.github.gatimus.equestrianbeats.action.stop";

    public static final String KEY_STREAM_URL = "stream_url";

    private static final String TAG = "MusicService";

    private MediaPlayer player;
    private WifiManager.WifiLock wifiLock;
    private MediaSession mSession;

    @Override
    public void onCreate() {
        super.onCreate();
        mSession = new MediaSession(this, getClass().getSimpleName());
        setSessionToken(mSession.getSessionToken());
        mSession.setCallback(new MediaSessionCallback());
    }

    private final class MediaSessionCallback extends MediaSession.Callback{

    }

    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid, Bundle rootHints) {
        return new BrowserRoot(MEDIA_ID_ROOT, rootHints);
    }

    @Override
    public void onLoadChildren(String parentId, final Result<List<MediaBrowser.MediaItem>> result) {
        switch (parentId){
            case MEDIA_ID_ROOT :
                List<MediaBrowser.MediaItem> mediaItems = new ArrayList<MediaBrowser.MediaItem>();
                mediaItems.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                        .setMediaId(MEDIA_ID_FEATURED)
                        .setTitle(MEDIA_ID_FEATURED)
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));
                mediaItems.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                        .setMediaId(MEDIA_ID_LATEST)
                        .setTitle(MEDIA_ID_LATEST)
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));
                mediaItems.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                        .setMediaId(MEDIA_ID_RANDOM)
                        .setTitle(MEDIA_ID_RANDOM)
                        .build(), MediaBrowser.MediaItem.FLAG_BROWSABLE));
                result.sendResult(mediaItems);
                break;
            case MEDIA_ID_FEATURED :
                EQBeats.getEQBInterface().getFeaturedTracks(new TrackCallback(result));
                result.detach();
                break;
            case MEDIA_ID_LATEST :
                EQBeats.getEQBInterface().getLatestTracks(new TrackCallback(result));
                result.detach();
                break;
            case MEDIA_ID_RANDOM :
                EQBeats.getEQBInterface().getRandomTracks(new TrackCallback(result));
                result.detach();
                break;
            default:
                result.sendResult(new ArrayList<MediaBrowser.MediaItem>()); //empty
        }


    }

    @Override
    public void onDestroy() {
        stopPlayback();
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            switch(intent.getAction()) {
                case ACTION_PLAY:
                    if(player != null) {
                        if(player.isPlaying()) break;
                        player.start();
                        break;
                    }
                    if(intent.hasExtra(KEY_STREAM_URL)) {
                        playRemoteResource(intent.getStringExtra(KEY_STREAM_URL));
                    }
                    break;
                case ACTION_PAUSE:
                    if(player != null && player.isPlaying())
                        player.pause();
                    break;
                case ACTION_STOP:
                    if(player != null && player.isPlaying())
                        stopPlayback();
                    break;
            }
        }


        return START_STICKY;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO: Actually handle errors, this is just generated for now
        return false;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // TODO: We've gained audio focus, handle it
                if(player != null) {
                    if(!player.isPlaying()) player.start();
                    player.setVolume(1.0f, 1.0f);
                } else {
                    // TODO: Init the player?
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                stopPlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                player.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if(player != null) {
                    player.setVolume(0.3f, 0.3f);
                }
                break;
        }
    }

    private void constructPlayer() {
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
    }

    private void requestAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if(result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // TODO: We need to handle the case where we can't get audio focus
        }
    }

    private void stopPlayback() {
        if(player != null) {
            player.stop();
            player.release();
            player = null;
        }

        if(wifiLock != null)
            if(wifiLock.isHeld()){
                wifiLock.release();
                wifiLock = null;
            }

    }


    private void playRemoteResource(String url) {
        constructPlayer();
        requestAudioFocus();
        try {
            player.setDataSource(url);
        } catch (IllegalStateException | IOException e) {
            stopPlayback();
        }
        player.prepareAsync();

        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "musicservice");
    }

    private class TrackCallback implements Callback<List<Track>> {

        private Result<List<MediaBrowser.MediaItem>> result;

        public TrackCallback(Result<List<MediaBrowser.MediaItem>> result){
            this.result = result;
        }

        @Override
        public void success(List<Track> tracks, Response response) {
            List<MediaBrowser.MediaItem> mediaItems = new ArrayList<MediaBrowser.MediaItem>();
            for(Track track : tracks){
                mediaItems.add(new MediaBrowser.MediaItem(new MediaDescription.Builder()
                        .setMediaId(String.valueOf(track.id))
                        .setTitle(track.title)
                        .setSubtitle(track.artist.name)
                        .setIconUri(track.download.get("art"))
                        .build(), MediaBrowser.MediaItem.FLAG_PLAYABLE));
                /*
                MediaMetadata mediaMetadata = new MediaMetadata.Builder()
                        .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, String.valueOf(track.id))
                        .putString(MediaMetadata.METADATA_KEY_TITLE, track.title)
                        .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, track.title)
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, track.artist.name)
                        .putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, track.artist.name)
                        .putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, track.description)
                        .putString(MediaMetadata.METADATA_KEY_ART_URI, track.download.get("art"))
                        .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, track.download.get("art"))
                        .putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, track.download.get("art"))
                        .build();
                mediaItems.add(new MediaBrowser.MediaItem(mediaMetadata.getDescription(), MediaBrowser.MediaItem.FLAG_PLAYABLE));
                */
            }
            result.sendResult(mediaItems);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(getClass().getSimpleName(), error.toString());
            result.sendResult(new ArrayList<MediaBrowser.MediaItem>());
        }
    }
}


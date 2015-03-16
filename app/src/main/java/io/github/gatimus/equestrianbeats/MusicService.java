package io.github.gatimus.equestrianbeats;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, AudioManager.OnAudioFocusChangeListener {

    public static final String ACTION_PLAY = "io.github.gatimus.equestrianbeats.action.play";
    public static final String ACTION_PAUSE = "io.github.gatimus.equestrianbeats.action.pause";
    public static final String ACTION_STOP = "io.github.gatimus.equestrianbeats.action.stop";

    public static final String KEY_STREAM_URL = "stream_url";

    private static final String TAG = "MusicService";

    private MediaPlayer player;
    private WifiManager.WifiLock wifiLock;

    public MusicService() {
    }

    @Override
    public void onDestroy() {
        stopPlayback();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
}


package com.diegeilstegruppe.sasha.Audio;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

/**
 * Created by denys on 19/05/2017.
 */

public class SpeechRecorder {

    private static final String LOG_TAG = "SpeechRecorder";

    private static String mFileName = null;

    private WavAudioRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    public static String getFileName() {
        return mFileName;
    }

    public SpeechRecorder(Context context) {

        mFileName = context.getCacheDir().getAbsolutePath();
        mFileName += "/audio.wav";
    }

    public void startRecording() {
        //int audioSource, int sampleRate, int channelConfig, int audioFormat
        mRecorder = WavAudioRecorder.getInstance();
        mRecorder.setOutputFile(mFileName);

        try {
            mRecorder.prepare();
        } catch (Exception e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        //mRecorder.prepare();
        mRecorder.start();
    }

    public void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    public void stopAndPlayRecording() {
        this.stopRecording();
        this.startPlaying();
    }

    public void onActivityStop() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }
}

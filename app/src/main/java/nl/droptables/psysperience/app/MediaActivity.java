package nl.droptables.psysperience.app;

import android.app.Activity;
import android.media.*;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.media.audiofx.Equalizer;

import java.io.IOException;

import static android.media.AudioFormat.*;


public class MediaActivity extends Activity {

    private static final String LOG_TAG = "AudioRecord:";
    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;
    private AudioRecord recorder = null;
    private String tmpaudiofile = null;
    private AudioTrack track = null;

    public MediaActivity() {
        tmpaudiofile = Environment.getExternalStorageDirectory().getAbsolutePath();
        tmpaudiofile += "/tmpaudio.aac";
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        try{
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "record set audiosource failed");
        }

        try{
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "record set outputformat failed");
        }

        try{
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "record set audioencode failed");
        }

        try{
//            mRecorder.setOutputFile(tmpaudiofile);
            mRecorder.setOu
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "record set outputfile failed");
        }

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "record record prepare() failed");
        }
        try{
            mRecorder.start();
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "record start failed");
        }
        try{
            mRecorder.release();
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "record release failed");
        }
    }

    private void startPlaying() {
//        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
//                CHANNEL_IN_MONO,
//                ENCODING_PCM_16BIT, 2);
//        track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
//                CHANNEL_OUT_MONO,
//                ENCODING_PCM_16BIT, 2,
//                AudioTrack.MODE_STREAM);
//        track.play();

        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(tmpaudiofile);

        } catch (IOException e) {
            Log.e(LOG_TAG, "play setdatasource failed");
        }

        try {
            mPlayer.prepare();

        } catch (IOException e) {
            Log.e(LOG_TAG, "play prepare() failed");
        }

        try {
            mPlayer.start();

        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "play start failed");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        Button chirp = (Button)this.findViewById(R.id.short_media);
        Button music = (Button)this.findViewById(R.id.long_media);
        final MediaPlayer mp_chirp = MediaPlayer.create(this, R.raw.chirp);
        final MediaPlayer mp_music = MediaPlayer.create(this, R.raw.music);
        mp_chirp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp_music.setAudioStreamType(AudioManager.STREAM_MUSIC);

        chirp.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
//                startRecording();
//                startPlaying();
                mp_chirp.start();
//                mp_chirp.setLooping(true);

            }
        });
        music.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                mp_music.start();
                // equalizer experiment
//                Equalizer eq = new Equalizer(0, mp_music.getAudioSessionId());
//                eq.setEnabled(true);
//                short bands = eq.getNumberOfBands();
//
//                final short minEQLevel = eq.getBandLevelRange()[0];
//                final short maxEQLevel = eq.getBandLevelRange()[1];
//
//                eq.setBandLevel(bands, (short) 1);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_media, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

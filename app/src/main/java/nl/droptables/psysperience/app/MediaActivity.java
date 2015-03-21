package nl.droptables.psysperience.app;

import android.app.Activity;
import android.content.res.TypedArray;
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
import java.util.Random;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        playBackground();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        Button music = (Button)this.findViewById(R.id.long_media);

        music.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playRandom();
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

    public void playBackground(){
        final MediaPlayer mp_background = MediaPlayer.create(this, R.raw.background_sound);
        mp_background.setVolume(0.5f, 0.5f);
        mp_background.start();
    }

    public void playRandom(){
        // load all the sounds! BTW, it's only 01:24 :')
        final MediaPlayer mp_0 = MediaPlayer.create(this, R.raw.a);
        final MediaPlayer mp_1 = MediaPlayer.create(this, R.raw.b);
        final MediaPlayer mp_2 = MediaPlayer.create(this, R.raw.c);
        final MediaPlayer mp_3 = MediaPlayer.create(this, R.raw.d);
        final MediaPlayer mp_4 = MediaPlayer.create(this, R.raw.e);
        final MediaPlayer mp_5 = MediaPlayer.create(this, R.raw.f);
        final MediaPlayer mp_6 = MediaPlayer.create(this, R.raw.g);
        final MediaPlayer mp_7 = MediaPlayer.create(this, R.raw.h);
        final MediaPlayer mp_8 = MediaPlayer.create(this, R.raw.i);
        final MediaPlayer mp_9 = MediaPlayer.create(this, R.raw.j);

        int randomInt = this.randInt(0,9);

        switch (randomInt) {
            case 0:
                mp_0.start();
                break;
            case 1:
                mp_1.start();
                break;
            case 2:
                mp_2.start();
                break;
            case 3:
                mp_3.start();
                break;
            case 4:
                mp_4.start();
                break;
            case 5:
                mp_5.start();
                break;
            case 6:
                mp_6.start();
                break;
            case 7:
                mp_7.start();
                break;
            case 8:
                mp_8.start();
                break;
            case 9:
                mp_9.start();
                break;
        }
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
}

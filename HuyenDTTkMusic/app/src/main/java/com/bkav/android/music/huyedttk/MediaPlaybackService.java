package com.bkav.android.music.huyedttk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Random;
import java.util.concurrent.TimeUnit;


public class MediaPlaybackService extends Service {   //luu lai id va Barbar phat nhac den dau, khi vao lan 2.
    private MediaPlayer mPlayer;

    private long[] mListId, mNewListId;

    private int mCurrentPosition;
    private long mCurrenId;


    private static final int NOTIFICATION_ID = 1;
    private final String CHANNEL_ID = "CHANNEL_ID";

    private MediaSessionCompat mediaSession;

    //------------
    private Cursor mCursor;

    /*
        Bitmap albumArtBitmap;
    */
    private NotificationManagerCompat managerCompat;
    //----------
    private boolean mNotifiPauseBtn = true;

    private RemoteViews mNotificationLayout, mNotificationLayoutExpanded;

    private NotificationCompat.Builder mBuilder;

    private boolean mShowBtnPause = true;

    //Define Notification Manager
    private NotificationManager mNotificationManager;

    //----------
    private boolean mCheckAppClose = false;

    //----------
    private String mCheckRepeat;
    private boolean mCheckShuffle;

    //----------
    private static final String NO_REPEAT = "NO_REPEAT";
    private static final String REPEAT_ALL = "REPEAT_ALL";
    private static final String REPEAT_ONE = "REPEAT_ONE";

    //----------
    private static final String NEXT_ACTION = "NEXT_ACTION";
    private static final String PAUSE_ACTION = "PAUSE_ACTION";
    private static final String PREV_ACTION = "PREV_ACTION";
    private static final String NEXT_ACTION_EXPANDED = "NEXT_ACTION_EXPANDED";
    private static final String PAUSE_ACTION_EXPANDED = "PAUSE_ACTION_EXPANDED";
    private static final String PREV_ACTION_EXPANDED = "PREV_ACTION_EXPANDED";

    //----------
    private static final String NEXT_ID = "NEXT_ID";
    private static final String SHOW_BTN_PAUSE = "SHOW_BTN_PAUSE";

    //----------

    private static final int STOP_SONG = -1;

    //-----------
    private MusicConstant mConstant = new MusicConstant();

    @Override
    public void onCreate() {
        super.onCreate();

        mPlayer = new MediaPlayer();

    }

    // Receiver event ;app close
    private BroadcastReceiver appCloseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("appClose----------", "appClose:huyen " + intent);

            if (intent.getAction().equals("appClose")) {
                Toast.makeText(context, "appClose", Toast.LENGTH_SHORT).show();

                mCheckAppClose = true;
            }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.i("onBind Service", "---------------onBind Service");


        return mBindler;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("onUnbind Service", "---------------onUnbind Service");

        return super.onUnbind(intent);
    }

    private IMusicService.Stub mBindler = new IMusicService.Stub() {

        @Override
        public void playSong(int pos) throws RemoteException {
            if (pos != STOP_SONG) {

                mPlayer.reset();
                mPlayer.release();

                mCurrentPosition = pos;    //click vao list bh lan 2
                Log.e("Thread name play song:", Thread.currentThread().getName());

                if (mCheckShuffle == false) {
                    mCurrenId = mListId[pos];   //lay id lan dau tien
                }

                String[] projection = {
                        MediaStore.Audio.AudioColumns._ID,
                        MediaStore.Audio.AudioColumns.DATA,
                        MediaStore.Audio.AudioColumns.ALBUM_ID,
                        MediaStore.Audio.AudioColumns.TITLE,
                        MediaStore.Audio.AudioColumns.ARTIST
                };

                mCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, MediaStore.Audio.AudioColumns.IS_MUSIC + "=" + 1, null, null);

                if (mCursor != null && mCursor.moveToFirst()) {
                    if (mCheckShuffle == false) {
                        mCursor.moveToPosition(pos);

                    } else {
                        for (int i = 0; i < mListId.length; i++) {
                            if (mListId[i] == mCurrenId) {
                                mCursor.moveToPosition(i);
                            }
                        }
                    }

                    Uri uri = Uri.parse(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));

                    mPlayer = MediaPlayer.create(getApplicationContext(), uri);
                    mPlayer.start();

                    sendSongId();

                    nextSongWhenCompletion();
                }
            }

        }

        @Override
        public void pause() throws RemoteException {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();

            }
        }

        @Override
        public void start() throws RemoteException {
            if (!mPlayer.isPlaying()) {
                mPlayer.start();
            }
        }

        @Override
        public String getSongTotalTime() throws RemoteException {
            int duration = mPlayer.getDuration();

            // convert time
            return String.format("%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(duration),
                    TimeUnit.MILLISECONDS.toSeconds(duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
            );
        }

        @Override
        public boolean getStatusMusic() throws RemoteException {
            if (mPlayer.isPlaying()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int getSongDuration() throws RemoteException {
            return mPlayer.getDuration();
        }

        @Override
        public boolean checkPlayerNull() throws RemoteException {
            if (mPlayer == null) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void setListId(long[] listId) throws RemoteException {
            mListId = listId;

            int index = 0;
            for (long value : mListId) {
                Log.i("check list id", "" + mListId[index++]);
            }
        }

        @Override
        public int getSongCurrentPosition() throws RemoteException {
            return mPlayer.getCurrentPosition();
        }


    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("onStartCommand", "onStartCommand: ");
        //return super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }


    //----------------------
    @Override
    public void onDestroy() {    //huy tranh truong hop Service onDestroy roi mak mPlayer van phat nhac.
        super.onDestroy();

        mPlayer.release();

        Log.i("onDestroy Service", "---------------onDestroy Service");

    }

    //----------------
    public void nextSongWhenCompletion() {

        Log.e("nextSongWhenCompletion", "nextSongWhenCompletion -------------------1234");

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                if (mPlayer != null) {
                    try {

                        mBindler.playSong(mCurrentPosition);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }

    //-----------------------
    // Send an Intent with an action named "updateUI". The Intent sent should  be received by the ActivityMusic.
    private void sendSongId() {
        Log.d("sender", "sender-------------------");

        Intent intent = new Intent("updateUI");

        intent.putExtra("nextId", mCurrenId);

        intent.putExtra("mShowBtnPause", mShowBtnPause);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}












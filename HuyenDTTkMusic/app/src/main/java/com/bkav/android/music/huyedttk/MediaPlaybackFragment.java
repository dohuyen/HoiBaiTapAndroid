package com.bkav.android.music.huyedttk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;

import static androidx.core.app.ServiceCompat.stopForeground;

public class MediaPlaybackFragment extends BaseFragment implements View.OnClickListener {

    private ImageView mSongSumAlbumImageIV, mSongAlbumImageIV;
    private TextView mSongSumSongTitleTV, mSongTimeBeginTV, mSongTimeEndTV, mSongSumAlbumNameTV;
    private ImageView mSongSkipPreviousIV, mSongPauseIV, mSongSkipNextIV, mSongRepeatIV, mSongShuffleIV, mSongLikeIV;
    private SeekBar mSeekBar;
    private int mDuration = 0, mPositionSeekBar;

    //-----------
    private MusicConstant mConstant = new MusicConstant();

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
            Log.d("check", "------run: " + mPositionSeekBar);
            Log.d("check", "------run: " + timeFormat.format(mPositionSeekBar));

            mSongTimeBeginTV.setText(timeFormat.format(mPositionSeekBar));


            Log.d("--------------- ", "-----------------------------");
            Log.d("check", "------run: " + mDuration + "----------" + mPositionSeekBar);

            //----finish seekbar
            handleFinishSeekBar();

            //update SeekBar
            mSeekBar.setProgress(mPositionSeekBar);

            mPositionSeekBar = mPositionSeekBar + 1000;

            mHandler.postDelayed(this, 1000);   //goi lai run ()

        }

    };

    public void handleFinishSeekBar() {
        if (mDuration <= mPositionSeekBar) {
            mPositionSeekBar = 0;
            mSongPauseIV.setImageResource(R.drawable.ic_fab_play_btn_normal);

            mHandler.removeCallbacks(mRunnable);
            return;
        }

    }

    //------------
    private String mTitle, mAlbumName, mAlbumArt, mTotalTime;
    private boolean mCheckStatusFalse;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(" onAttach", "---------onAttach MEDIA");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateSeekBar();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Đọc file xml tạo ra đối tượng View.

        final View view = inflater.inflate(R.layout.song_detail_fragment, container, false);

        mSongSumAlbumImageIV = view.findViewById(R.id.songSumAlbumImageIV);
        mSongAlbumImageIV = view.findViewById(R.id.songAlbumImageIV);

        mSongSumSongTitleTV = view.findViewById(R.id.songSumSongTitleTV);
        mSongTimeBeginTV = view.findViewById(R.id.songTimeBeginTV);
        mSongTimeEndTV = view.findViewById(R.id.songTimeEndTV);
        mSongSumAlbumNameTV = view.findViewById(R.id.songSumAlbumNameTV);

        mSongSkipPreviousIV = view.findViewById(R.id.songSkipPreviousIV);
        mSongSkipNextIV = view.findViewById(R.id.songSkipNextIV);
        mSongPauseIV = view.findViewById(R.id.songPauseIV);
        //--------
        mSongRepeatIV = view.findViewById(R.id.songRepeatIV);
        mSongShuffleIV = view.findViewById(R.id.songShuffleIV);

        //--------
        mSeekBar = view.findViewById(R.id.seek_bar);  //chua xet mau seekbar; da cam.

        //--------
        mSongLikeIV = view.findViewById(R.id.songLikeIV);

        //------
        mSongSkipPreviousIV.setOnClickListener(this);
        mSongSkipNextIV.setOnClickListener(this);
        mSongPauseIV.setOnClickListener(this);


        //------
        mSongRepeatIV.setOnClickListener(this);
        mSongShuffleIV.setOnClickListener(this);
        //------
        mSongLikeIV.setOnClickListener(this);


        Log.e("CREATE VIEW ", " CREATE VIEW MEDIAPLAYBACK");
        //------
        updateView();

        //------

        return view;
    }

    //---------------------
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.songPauseIV:

        }
    }

    //--------------------


    //---------------------
    public void updateView() {

        if (getArguments() != null) {
 Long songId = getArguments().getLong("song_id");

            mTitle = getArguments().getString(mConstant.SONG_TITLE);
            mAlbumName = getArguments().getString(mConstant.SONG_ALBUM);
            mAlbumArt = getArguments().getString(mConstant.SONG_ALBUM_ART);
            mTotalTime = getArguments().getString(mConstant.SONG_TIME_END);
            mCheckStatusFalse = getArguments().getBoolean(mConstant.SONG_BTN_STATUS);
            mDuration = getArguments().getInt(mConstant.SONG_DURATION);
            mPositionSeekBar = getArguments().getInt(mConstant.SONG_CURRENT_POSITION);

            //----------------


            //-------------- create view ------------
            mSongSumAlbumImageIV.setImageURI(Uri.parse(mAlbumArt));
            mSongSumSongTitleTV.setText(mTitle);
            mSongSumAlbumNameTV.setText(mAlbumName);

            mSongAlbumImageIV.setImageURI(Uri.parse(mAlbumArt));
            mSongTimeEndTV.setText(mTotalTime);

            if (mCheckStatusFalse == true) {
                mSongPauseIV.setImageResource(R.drawable.ic_fab_pause_btn_normal);
            } else {
                mSongPauseIV.setImageResource(R.drawable.ic_fab_play_btn_normal);
            }

            //------------update


        }
    }


    //---------------------------
    public void updateSeekBar() {
        mHandler.removeCallbacks(mRunnable);

        mSeekBar.setMax(mDuration);

        Log.d("check", "------run: mDuration" + mDuration);
        Log.d("check", "------run: mPositionSeekBar " + mPositionSeekBar);

        mHandler.postDelayed(mRunnable, 50); //Để thực thi một cái gì đó trong UI Thread sau 1/10 giây:
        Log.e("Thread name play song:", " updateSeekBar " + Thread.currentThread().getName());
    }

    public void updateSeekBarPause() {
        mHandler.removeCallbacks(mRunnable);

        mSeekBar.setMax(mDuration);

        Log.d("check", "------run: mDuration" + mDuration);
        Log.d("check", "------run: mPositionSeekBar " + mPositionSeekBar);

    }

    public SeekBar checkSeekBarExist() {
        return mSeekBar;
    }





    @Override
    public void onDetach() {
        super.onDetach();
        mHandler.removeCallbacks(mRunnable);
    }
}






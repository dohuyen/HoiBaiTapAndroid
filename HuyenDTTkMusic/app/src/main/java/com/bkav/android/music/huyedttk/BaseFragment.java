package com.bkav.android.music.huyedttk;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    //-------------
    public interface IActivityCommunicator{    //passDataToActivity
        void playSongSummary(int position);

/*
        void setDataToShowSongSummaryView(int position);
*/

        void handleBtnMain();

        void replaceFragment();

    }

    //-------------interface via which we communicate to hosting Activity
    private IActivityCommunicator mActivityCommunicator;

    //-------------
    public void setObject(IActivityCommunicator object){
        mActivityCommunicator = object;
    }
    //-------------
    public void playMusicFromAllSongFragment( int position){

        if (mActivityCommunicator != null) {
            mActivityCommunicator.playSongSummary(position);
        }
    }
    //-------------
/*    public void showSummaryInAllSongFragment( int position){

        if (mActivityCommunicator != null) {
            mActivityCommunicator.setDataToShowSongSummaryView(position);
        }
    }*/
    //-------------
    public void clickBtnMainFromFragment() {
        if (mActivityCommunicator != null) {
            mActivityCommunicator.handleBtnMain();
        }
    }
    //-------------
    public void addMediaPlaybackFragment() {
        if (mActivityCommunicator != null) {
            mActivityCommunicator.replaceFragment();
        }
    }










}
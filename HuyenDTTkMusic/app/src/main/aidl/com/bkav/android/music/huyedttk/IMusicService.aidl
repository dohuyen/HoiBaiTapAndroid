// IMusicService.aidl
package com.bkav.android.music.huyedttk;

// Declare any non-default types here with import statements

interface IMusicService {
    void playSong(int pos);

    void pause();
    void start();

    String getSongTotalTime();

    boolean getStatusMusic();

    int getSongDuration();

    boolean checkPlayerNull();

    //--------------------

    void setListId(in long[] listId);

    int getSongCurrentPosition();

   }

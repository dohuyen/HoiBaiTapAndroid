package com.bkav.android.music.huyedttk;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//Hien thi 1 danh sach bh
public class AllSongsFragment extends BaseFragment implements ActivityMusic.IFragmentCommunicatorWithAllSong, View.OnClickListener {

    private RecyclerView mRecyclerView;
    private SongListAdapter mAdapter;

    private Cursor mCursor = null;    //test
    //----
    private View mSummarySongLayout;
    private ImageView mSummaryAlbumImageIV;
    private TextView mSummarySongTitleTV, mSummaryAlbumNameTV;
    private ImageButton mSummaryMainIB;

    //----
    private MusicConstant mConstant = new MusicConstant();
    //----
    private boolean mCheckToShowSongSummary = false; //de hien thi tom tat bh khi từ detail ấn back system.

    //---------------
    public AllSongsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(" onAttach", "---------onAttach");

        ((ActivityMusic) context).iFragmentCommunicatorWithAllSong = this;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(" onCreate ALL", "---------onCreate ALL");


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(" onCreateView ALL", "---------onCreateView ALL");

        // Đọc file xml tạo ra đối tượng View.
        View view = inflater.inflate(R.layout.song_fragment, container, false);

        // Create recycler view.
        mRecyclerView = view.findViewById(R.id.recyclerView);

        //dau gach - ngan cach tung item.
/*        RecyclerView.ItemDecoration divider = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(divider);*/

        mAdapter = new SongListAdapter(getContext(), null);
        mRecyclerView.setAdapter(mAdapter);

        // Give the recycler view a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //-------------------
        this.setDataCursor(mCursor);
        //-------------------
        //create view : summary song
        mSummarySongLayout = view.findViewById(R.id.summarySongLayout);

        return view;
    }

    //---------------
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSummaryAlbumImageIV = view.findViewById(R.id.summaryAlbumImageIV);
        mSummaryAlbumNameTV = view.findViewById(R.id.summaryAlbumNameTV);
        mSummarySongTitleTV = view.findViewById(R.id.summarySongTitleTV);
        mSummaryMainIB = view.findViewById(R.id.summaryPauseIB);
        //---------------------
        mSummaryMainIB.setOnClickListener(this);

        //---------------------
        mSummarySongLayout.setOnClickListener(this);

    }

    public void updateView(int pos) {         //---------------- create view for summary song
        /*      if (getArguments() != null) {*/
        if (mCursor != null) {
            mCursor.moveToPosition(pos);

            String mAlbumArt = String.valueOf((ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"),
                    mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID)))));
            if (mAlbumArt != null) {
                mSummaryAlbumImageIV.setImageURI(Uri.parse(mAlbumArt));
            }
            mSummarySongTitleTV.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE)));
            mSummaryAlbumNameTV.setText(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM)));
            /*  }*/

            mSummarySongLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i(" onActivityCreated ALL", "---------onActivityCreated ALL");

        //click item
        mAdapter.setOnItemClickListener(new SongListAdapter.ClickListener() {
            @Override
            public void onItemClick(View v, int pos) {

                updateView(pos);

                playMusicFromAllSongFragment(pos);    // play song & update song summary view

            }
        });

        //---------------- hien thi tom tat bh khi từ detail ấn back system
        if (mCheckToShowSongSummary) {
            mSummarySongLayout.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        Log.i(" onStart ALL", "---------onStart ALL");

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(" onResume ALL", "---------onResume ALL");
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(" onPause ALL", "---------onPause ALL");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.i(" onStop ALL", "---------onStop ALL");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.i(" onDestroyView ALL", "---------onDestroyView ALL");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(" onDestroy ALL", "---------onDestroy ALL");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        Log.i(" onDetach ALL", "---------onDetach ALL");
    }

    //--------------- set Cursor(data) into  adapter.
    public void setDataCursor(Cursor mCursor) {
        if (mAdapter != null) {
            this.mAdapter.setDataCursor(getContext(), mCursor);
        }
    }

    //FragmentCommunicator interface implementation
    @Override
    public void passDataToFragmentAllSong(Cursor cursor) {
        mCursor = cursor;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.summaryPauseIB:
                clickBtnMainFromFragment();
                break;

            case R.id.summarySongLayout:
                addMediaPlaybackFragment();     //Replace, call fragment detail
                break;

        }
    }

    //--------------- hien thi button main
    public void showBtnMain(boolean showPause) {
        if (showPause) {
            mSummaryMainIB.setImageResource(R.drawable.ic_pause);
        } else {
            mSummaryMainIB.setImageResource(R.drawable.ic_play);
        }
    }

    //--------------- hien thi view tom tat bh o list nhac
    public void showSongSummanyView() {

        if (mSummarySongLayout != null) {
            mSummarySongLayout.setVisibility(View.VISIBLE);
        }

        Log.i("showSongSummanyView ALL", "---------showSongSummanyView  ALL");

    }


    //--------------- hien thi view tom tat bh o list nhac


}
















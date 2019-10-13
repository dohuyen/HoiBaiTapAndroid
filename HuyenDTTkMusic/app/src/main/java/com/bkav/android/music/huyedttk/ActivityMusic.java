package com.bkav.android.music.huyedttk;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.navigation.NavigationView;

import static android.provider.MediaStore.Audio.AudioColumns.ALBUM_ID;

//object callback
public class ActivityMusic extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, BaseFragment.IActivityCommunicator, View.OnClickListener {
    private long[] mIdList;

    private Cursor mCursor;

    private IMusicService iMusicService;

    private int mCurrentPosition;

    /**
     * Flag indicating whether we have called bind on the service.
     */
    boolean bound = false;
    boolean isFragmentDisplayed = false;

    //loader
    private final static int LOADER_SONGS_ID = 1;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 10;

    //----
    private AllSongsFragment mAllSongsFragment;
    private MediaPlaybackFragment mPlaybackFragment;
    //----
    public IFragmentCommunicatorWithAllSong iFragmentCommunicatorWithAllSong;

    //----


    private boolean mShowBtnPause = true;
    //-----------
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

    //-----------
    private int mCountForListId = 0;

    //-----------
    private MusicConstant mConstant = new MusicConstant();

    //-----------
    private long mNextSongId = -1;

    //-----------
    //-----------
    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadExternalPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (hasReadExternalPermission != PackageManager.PERMISSION_GRANTED) {  // != 0
                //Register permission   //call Dialog request permision
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                return;
            }
            getSupportLoaderManager().initLoader(LOADER_SONGS_ID, null, this);
        }
    }

    //user choice Allow or Deny, run onRequestPermissionsResult() of Activity, return result throught grantResults:
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    /**
                     * Initializes the CursorLoader. The SONG_LOADER_ID value is eventually passed
                     * to onCreateLoader().
                     */

                    getSupportLoaderManager().initLoader(LOADER_SONGS_ID, null, this);

                    Toast.makeText(this, "Allow", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Deny", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true;
    }

    //-------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mActionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(" onCreate ACTIVITY", "---------onCreate  ACTIVITY");

        //------------------
        setContentView(R.layout.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //------------------
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.bringToFront();
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.listen_now:
                        Toast.makeText(ActivityMusic.this, "Listen Now", Toast.LENGTH_SHORT).show();

                        break;
                    case R.id.recents:
                        Toast.makeText(ActivityMusic.this, "Recents", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.music_library:
                        Toast.makeText(ActivityMusic.this, "Music library", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        return true;
                }
                return true;
            }
        });

        //---------------------
        if (!isFragmentDisplayed) {
            addAndReplaceFragment(mAllSongsFragment);         //add fragment into activity
        }

        //---------------------
/*
        // Register to receive data with actions named "updateUI".
        LocalBroadcastManager.getInstance(this).registerReceiver(nextSongReceiver,
                new IntentFilter("updateUI"));

*/

        //-----------Start Service when unBind
        Intent intent = new Intent(this, MediaPlaybackService.class);
        startService(intent);

    }

    //-------------------

    //method : add and replace fragment into frameContent
    public void addAndReplaceFragment(Fragment fragment) {
        Log.i("-addAndReplaceFragment", "--------------------addAndReplaceFragment");

        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        mAllSongsFragment = new AllSongsFragment();
        mPlaybackFragment = new MediaPlaybackFragment();

        if (getSupportFragmentManager().findFragmentById(R.id.frameContent) == null) {
            mAllSongsFragment.setObject(this); // this la main activity ,cung chinh la Object cua Interface. , setObject khi khoi tao Fragment

            fragmentTransaction.add(R.id.frameContent, mAllSongsFragment, "frag1");
            //---------dang sua 10/10

        }else {
            mPlaybackFragment.setObject(this);  //tb cho Fragment la Activity laf object callback.
            fragmentTransaction.replace(R.id.frameContent, mPlaybackFragment, "frag2");

            fragmentTransaction.addToBackStack(null);
       /*     mPlaybackFragment.setArguments(bundle);*/


        }

/*        String extras = getIntent().getStringExtra("KEY");    //---------quen mat tac dung :v :v
        if (extras != null && extras.equals("KEY")) {
            {
                fragmentTransaction.replace(R.id.frameContent, mPlaybackFragment, "frag2");
            }
        }*/

        fragmentTransaction.commit();

    }

/*    //---------------
    // Our handler for received Intents. This will be called whenever an Intent with an action named "updateUI" is broadcasted.
    private BroadcastReceiver nextSongReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            mNextSongId = intent.getLongExtra("nextId", -1);

            Log.d("receiver", "----------------------------------");
            Log.d("receiver", "next song has id : " + mNextSongId);
            Log.d("receiver", "next song has id : " + mCurrentPosition);
            Log.d("receiver", "----------------------------------");

            transferDataFromDetail();    //update UI when click next/pre from Detail Song

            //--------
            mComplete = true;

            //--------


        }
    };*/


    //--------------- Connect/Disconnect to Service ---------------
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // cast sang interface de goj ham play

            iMusicService = IMusicService.Stub.asInterface(iBinder);
            bound = true;

            //-------------
            initPermission();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            iMusicService = null;
            bound = false;

        }
    };

    //---------------
    @Override
    protected void onStart() {
        super.onStart();

        Log.i(" onStart ACTIVITY", "---------onStart  ACTIVITY");

        // Bind to the service
        Intent intent = new Intent(this, MediaPlaybackService.class);
        this.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(" onResume ACTIVITY", "---------onResume  ACTIVITY" );

    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(" onPause ACTIVITY", "---------onPause  ACTIVITY");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(" onStop ACTIVITY", "---------onStop  ACTIVITY");
        // Unbind from the service
        if (bound) {
            this.unbindService(mConnection);
            bound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(" onDestroy ACTIVITY", "---------onDestroy  ACTIVITY");

    }

    @Override
    public void onBackPressed() {    // handler back system --> hien thi view summary
        super.onBackPressed();
        Log.i(" onBackPressed ACTIVITY", "---------onBackPressed  ACTIVITY");



    }


    @NonNull
    @Override
    public androidx.loader.content.Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        String[] projection = {
                MediaStore.Audio.AudioColumns._ID,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.ALBUM_ID
        };

        CursorLoader loader = null;

        if (id == 1) {

            loader = new CursorLoader(this,
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    MediaStore.Audio.AudioColumns.IS_MUSIC + "=" + 1,
                    null,
                    null
            );
        }

        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull androidx.loader.content.Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == 1) {
            data.moveToFirst();

            Log.i("##############", " " + data);
            if (mAllSongsFragment != null) {
                Log.i(" tDataCursor", "----xx-----tDataCursor");

                mAllSongsFragment.setDataCursor(data);         //xet data len view
            }

            //----------
            if (iFragmentCommunicatorWithAllSong != null) {
                Log.i("++++++++++++", " =========================" + data);
                iFragmentCommunicatorWithAllSong.passDataToFragmentAllSong(data);

            }

            //add value into mIdList of Songs
            if ((data != null) && data.moveToFirst()) {
                //clear listId khi pause va vao lai
                /*   Arrays.asList(mIdList).clear();*/

                Log.i("++++++++++++", " =========================" + mIdList);

                if (mCountForListId >= data.getCount()) {
                    mCountForListId = 0;
                }

                //khoi tao mang mIdList
                mIdList = new long[data.getCount()];

                do {
                    mIdList[mCountForListId] = data.getLong(data.getColumnIndex(MediaStore.Audio.Media._ID));
                    mCountForListId = mCountForListId + 1;

                } while (data.moveToNext());
            }

            mCursor = data;

        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    //--------------- play song ---------------
    @Override
    public void playSongSummary(int position) {
        try { //play 2 : button play + click bh trong list. => phat nhac + button pause
            if (iMusicService.getStatusMusic() == false) {     //true
                mShowBtnPause = true;
            }

            iMusicService.setListId(mIdList);   //chuyen listId sang Service
            //===========
            iMusicService.playSong(position);

         /*   mAllSongsFragment.updateView();*/

            //===========

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //-----------------

    }


    //---------------
    @Override
    public void onClick(View view) {   //click from Main Activity
        switch (view.getId()) {


        }
    }

    //---------------
    @Override
    public void replaceFragment() {
        //Replace, call fragment detail
        MediaPlaybackFragment playbackFragment = new MediaPlaybackFragment();
        addAndReplaceFragment(playbackFragment);

    }

    //---------------
    @Override
    public void handleBtnMain() {
        clickBtnMain();

    }
    //--------------- handle button main (pause/play) ---------------
    public void clickBtnMain() {
        try {
        if (iMusicService.getStatusMusic()) {    //isRunning --> click thì hiện btn play và pause nhạc.
            mAllSongsFragment.showBtnMain(false);
            iMusicService.pause();

        }else{
            mAllSongsFragment.showBtnMain(true);    // ! isRunning --> click thì hiện btn pause và phát tiếp nhạc.
            iMusicService.start();

        }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    //------------------------------
    public interface IFragmentCommunicatorWithAllSong {    //passDataToFragment
        void passDataToFragmentAllSong(Cursor cursor);

    }
    //------------------------------





}

















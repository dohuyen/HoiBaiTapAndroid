package com.bkav.android.music.huyedttk;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.TimeUnit;

import static android.provider.MediaStore.Audio.AudioColumns.TITLE;

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder> {

    private Cursor mCursor;
    Context mContext;

    static ClickListener listenerInterface;

    private MusicUtil mUtil = new MusicUtil();

    public SongListAdapter(Context context, Cursor cursor) {
        mContext = context;
        this.mCursor = cursor;
    }

    class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


        public TextView songTitleView;
        public TextView songTimeView;

        public ImageView moreVertView;
        public TextView ordinalNumberView;

        public SongViewHolder(View itemView) {
            super(itemView);

            songTitleView = itemView.findViewById(R.id.songTitleTV);
            songTimeView = itemView.findViewById(R.id.songTimeTV);
            moreVertView = itemView.findViewById(R.id.actionMoreVertIV);
            ordinalNumberView = itemView.findViewById(R.id.ordinalNumberTV);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int pos = getAdapterPosition();

            //Log.e("-pos on SongListAdapter", "------------------------" + pos);
            listenerInterface.onItemClick(view, pos);  //dt nghe chung.

        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Log.i("check adpater---------", "=======================onCreateViewHolder");


        View mItemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_item, parent, false);
        return new SongViewHolder(mItemView);

    }
    
    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        //Log.i("check adpater---------", "=======================onBindViewHolder");

        mCursor.moveToPosition(position);

 /*       holder.ordinalNumberView.setText("" + (position + 1));
*/
        holder.ordinalNumberView.setText("" + mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media._ID)));  //dang sua 10/9

        holder.songTitleView.setText(mCursor.getString(mCursor.getColumnIndex(TITLE)));

        holder.songTimeView.setText(mUtil.getDuration(mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA))));
        holder.moreVertView.setImageResource(R.drawable.ic_more_vert);


    }

    public void setDataCursor(Context context, Cursor dataCursor) {
        this.mContext = context;
        this.mCursor = dataCursor;

        notifyDataSetChanged();   //cap nhat lai view

        //Log.i("check adpater---------", "=======================");

    }


    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public void setOnItemClickListener(ClickListener clickListener) {  //thong bao ai lang nghe. dk
        SongListAdapter.listenerInterface = clickListener;
    }

    public interface ClickListener {       //tao doi tuong ClickLítener
        void onItemClick(View v, int position);

    }




}


//adapter goi ngc vao MainActivity
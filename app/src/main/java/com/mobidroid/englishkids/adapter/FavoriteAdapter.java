package com.mobidroid.englishkids.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.mobidroid.englishkids.PlayActivity;
import com.mobidroid.englishkids.R;
import com.mobidroid.englishkids.item.KEY;
import com.mobidroid.englishkids.item.Video;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.MyHolder> {

    private List<Video> videoList;
    private Context context;
    private static final String TAG = "FavoriteAdapter";

    public FavoriteAdapter(List<Video> videoList, Context context) {
        this.videoList = videoList;
        this.context = context;
    }


    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_video_list, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        Video video = videoList.get(i);
        myHolder.band(video);
    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView image;
        public ImageView image_delete;
        private TextView tvTitle, tvDescription;
        public LikeButton likeHeart, likeStar;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            image         = itemView.findViewById(R.id.img_custom);
            image_delete  = itemView.findViewById(R.id.img_custom_delete);
            tvTitle       = itemView.findViewById(R.id.tv_title_custom);
            tvDescription = itemView.findViewById(R.id.tv_des_custom);

            likeHeart     = itemView.findViewById(R.id.heart_button);
            likeStar      = itemView.findViewById(R.id.star_button);

            itemView.setOnClickListener(this);
        }

        public void band(final Video video) {
            Glide.with(image.getContext())
                    //.onLoadFailed(R.drawable.image)
                    .load(video.getUri_video())
                    .apply(new RequestOptions()
                            //.placeholder(R.drawable.placeholder)
                            //.error()
                            .centerInside())
                    .into(image);

            tvTitle.setText(video.getTitle_video());
            tvDescription.setText(video.getDescription());

            final String STAR = "star";
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String jsonHeart = preferences.getString(video.getTitle_video().trim(), "");
            //SharedPreferences.Editor prefsEditor = preferences.edit();
            if (!jsonHeart.isEmpty()) {
                likeHeart.setLiked(true);
            }

            String jsonStar = preferences.getString(video.getTitle_video().trim()+STAR, "");
            if (!jsonStar.isEmpty()) {
                likeStar.setLiked(true);
            }

            likeHeart.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    try {
                        SharedPreferences.Editor prefsEditor = preferences.edit();
                        Gson gson = new Gson();
                        String json = gson.toJson(video);
                        prefsEditor.putString(video.getTitle_video().trim(), json);
                        prefsEditor.apply();
                        Log.d(TAG, "liked: ");
                    }catch (Exception e) {
                        Log.e(TAG, "liked: error "+e.getMessage());
                    }
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    try {
                        SharedPreferences.Editor prefsEditor = preferences.edit();
                        prefsEditor.remove(video.getTitle_video());
                        prefsEditor.apply();

                        notifyDataSetChanged();
                        MyHolder.this.notify();
                        Log.d(TAG, "unLiked: ");
                    }catch (Exception e) {
                        Log.e(TAG, "liked: error "+e.getMessage());
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            final Video video = videoList.get(position);
            Intent intent = new Intent(image.getContext(), PlayActivity.class);
            intent.putExtra(KEY.VIDEOS, video);
            intent.putExtra(KEY.POSITION, position);
            image.getContext().startActivity(intent);
        }
    }
}

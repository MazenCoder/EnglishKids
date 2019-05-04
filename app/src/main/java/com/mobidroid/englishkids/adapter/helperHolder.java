package com.mobidroid.englishkids.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.like.LikeButton;
import com.mobidroid.englishkids.R;
import com.mobidroid.englishkids.item.Courses;
import com.mobidroid.englishkids.item.Video;


public class helperHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "helperHolder";
    private ImageView image;
    public ImageView image_delete;
    private TextView tvTitle, tvDescription;
    public LikeButton likeHeart, likeStar;

    public helperHolder(@NonNull View itemView) {
        super(itemView);

        image         = itemView.findViewById(R.id.img_custom);
        image_delete  = itemView.findViewById(R.id.img_custom_delete);
        tvTitle       = itemView.findViewById(R.id.tv_title_custom);
        tvDescription = itemView.findViewById(R.id.tv_des_custom);

        likeHeart     = itemView.findViewById(R.id.heart_button);
        likeStar      = itemView.findViewById(R.id.star_button);
    }

    public void bind(Object model) {
        if (model != null) {

            Courses courses = new Courses();
            Video video = new Video();

            if (model.getClass().isInstance(courses)) {
                courses = (Courses) model;
                try {

                    Glide.with(image.getContext())
                            .load(courses.getImage())
                            .apply(new RequestOptions()
                                    //.placeholder(R.drawable.placeholder)
                                    //.error()
                                    .centerInside())
                            .into(new DrawableImageViewTarget(image));

                    tvTitle.setText(courses.getTitle());
                    tvDescription.setText(courses.getDescription());

                }catch (Exception e) {
                    Log.e(TAG, "bind course method: error "+e.getMessage());
                }
            }else if (model.getClass().isInstance(video)) {
                video = (Video) model;
                try {

                    Glide.with(image.getContext())
                            .load(video.getUri_video())
                            .apply(new RequestOptions()
                                    //.placeholder(R.drawable.placeholder)
                                    //.error()
                                    .centerInside())
                            .into(new DrawableImageViewTarget(image));

                    tvTitle.setText(video.getTitle_video());
                    tvDescription.setText(video.getDescription());

                }catch (Exception e) {
                    Log.e(TAG, "bind video method: error "+e.getMessage());
                }
            }

            /**
            if (model instanceof Courses) {
                //Courses courses = (Courses) model;
                try {
                    Picasso.get()
                            .load(courses.getImage())
                            .centerCrop()
                            .fit()
                            .into(image);

                    tvTitle.setText(courses.getTitle());
                    tvDescription.setText(courses.getDescription());
                }catch (Exception e) {
                    Log.e(TAG, "bind course method: error "+e.getMessage());
                }
            } else if (model instanceof Video){
                Video video = (Video) model;
                try {
                    Glide.with(image.getContext())
                            .asBitmap()
                            .load(Uri.fromFile(new File(video.getUri_video())))
                            .into(image);

                    tvTitle.setText(video.getTitle_vidoe());
                    tvDescription.setText(video.getDescription());
                }catch (Exception e) {
                    Log.e(TAG, "bind video method: error "+e.getMessage());
                }
            }
            */
        }

    }


}

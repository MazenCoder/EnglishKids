package com.mobidroid.englishkids.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

//import com.bumptech.glide.Glide;

import com.mobidroid.englishkids.R;
import com.squareup.picasso.Picasso;


import java.util.ArrayList;
import java.util.List;

public class SlideAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater inflater;
    public List<String> uriList = new ArrayList<>();

//    public int[] images = {
//            R.drawable.image,
//            R.drawable.image,
//            R.drawable.image,
//            R.drawable.image,
//            R.drawable.image
//    };

    public SlideAdapter(Context context, List<String> uriList) {
        this.context = context;
        this.uriList = uriList;
    }

    @Override
    public int getCount() {
        return uriList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return (view == (LinearLayout) o);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.slide_adapter, container, false);

        ImageView imageView = (ImageView) view.findViewById(R.id.img_slide_adapter);
//        imageView.setImageResource(images[position]);

        Picasso.get().load(uriList.get(position)).fit().centerCrop().into(imageView);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }
}

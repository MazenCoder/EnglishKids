package com.mobidroid.englishkids.adapter;

import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mobidroid.englishkids.R;
import com.mobidroid.englishkids.item.Images;
import com.mobidroid.englishkids.item.KEY;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyHolder> {

    private static final String TAG = "ImageAdapter";
    //List<String> imageList = new ArrayList<>();
    private List<String> uriList = new ArrayList<>();
    private List<String> keyList = new ArrayList<>();
    private FirebaseFirestore db;
    private DocumentReference documentReference;
    private Images images;

    public ImageAdapter(Images imageList) {
        this.images = imageList;
        //this.imageList = imageList;

        db = FirebaseFirestore.getInstance();

        String uri;
        if (images.getImages_uri_map() != null && images.getImages_uri_map().size() != 0) {
            Set<Map.Entry<String, String>> value = images.getImages_uri_map().entrySet();
            for (Map.Entry<String, String> entry : value) {
                Log.d(TAG, "bindAds: get key" + entry.getKey() + "  get Values" + entry.getValue());
                uri = images.getImages_uri_map().get(entry.getKey());
                keyList.add(entry.getKey());//  get key
                Log.d(TAG, "ImageAdapter: Key: "+entry.getKey());
                uriList.add(uri);// get Value
                Log.d(TAG, "onSuccess: "+uri);
            }
            Log.d(TAG, "onSuccess: uris: "+uriList.size());
        }
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_row, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String img = uriList.get(position);
//        Images images = imageList.get(position);
        holder.bind(img, position);
    }

    @Override
    public int getItemCount() {
        return uriList.size();
    }


    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView image_display, image_close;
        private View view;

        public MyHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            image_display = (ImageView)itemView.findViewById(R.id.image_display);
            image_close   = (ImageView)itemView.findViewById(R.id.image_close);
            image_close.setOnClickListener(this);
        }

        public void bind(String img, int position) {
            Picasso.get().load(img)
                    .fit().centerCrop()
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(image_display);

            //images.setPosition(position);
        }

        @Override
        public void onClick(View view) {
            try {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(image_display.getContext(), android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(image_display.getContext());
                }
                builder.setTitle("Delete Image")
                        .setMessage("Are you sure you want to delete this image?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                                deleteImage();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }catch (Exception e) {
                Log.e(TAG, "onClick: error "+e.getMessage());
            }
        }

        private void deleteImage() {
            try {
                final int position = getAdapterPosition();
                Log.d(TAG, "onClick: "+position);
                FirebaseStorage storage = FirebaseStorage.getInstance();

                StorageReference photoRef = storage.getReferenceFromUrl(uriList.get(position));
                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // File deleted successfully
                        Log.d(TAG, "onSuccess: deleted file");

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put(KEY.IMAGES_MAP+"."+keyList.get(position), FieldValue.delete());

                        documentReference = db.collection(KEY.IMAGES).document(KEY.IMAGES);

                        documentReference.update(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: ");
//                                    Check.MessageSnackBarShort(view.findViewById(R.id.coordinatorLayout_add_course),
//                                            image_display.getContext().getString(R.string.deleted));

                                    Toast.makeText(image_display.getContext(), image_display.getContext().getString(R.string.deleted), Toast.LENGTH_SHORT).show();
//                                    AddCourseActivity addCourseActivity = new AddCourseActivity();
//                                    addCourseActivity.getImages();
//                                    notifyItemInserted(position);
                                    notifyItemChanged(position);
                                    notifyDataSetChanged();

                                }else {
                                    Log.e(TAG, "onComplete: error "+task.getException().getMessage());
                                }
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Uh-oh, an error occurred!
                        Log.d(TAG, "onFailure: did not delete file");
                    }
                });

            }catch (Exception e) {
                Log.e(TAG, "removeFavorite_list: error "+e.getMessage());
            }
        }
    }
}

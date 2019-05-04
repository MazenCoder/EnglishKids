package com.mobidroid.englishkids;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.mobidroid.englishkids.adapter.FavoriteAdapter;
import com.mobidroid.englishkids.adapter.helperHolder;
import com.mobidroid.englishkids.item.KEY;
import com.mobidroid.englishkids.item.Video;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FavoriteActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "FavoriteActivity";
    private List<Video> videoList = new ArrayList<>();
    private RecyclerView recyclerView;
    //private FirestoreRecyclerAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private Toolbar toolbar;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        FirebaseMessaging.getInstance().subscribeToTopic("TOPIC_ENGLISH_FUN");
        recyclerView = findViewById(R.id.recycler_view_favorite);
        preferences = PreferenceManager.getDefaultSharedPreferences(FavoriteActivity.this);
        toolbar = findViewById(R.id.toolbar_favorite);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_back_home);
        upArrow.setColorFilter(getResources().getColor(R.color.White), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        refreshLayout = findViewById(R.id.swipe_refresh_favorite);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_red_light,
                R.color.black,
                R.color.yellow,
                android.R.color.holo_red_light);

        queryData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_video_menu, menu);
        return true;
//        getMenuInflater().inflate(R.menu.list_video_menu, menu);
//        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = preferences.edit();
        switch (item.getItemId()) {
            case R.id.action_layout_staggered_one: {
                editor.putBoolean(KEY.STAGGERED_ONE, true);
                //boolean staggered = preferences.getBoolean(KEY.STAGGERED_ONE, false);
                Log.d(TAG, "onOptionsItemSelected: staggered one: ");
                LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
                        LinearLayoutManager.VERTICAL, false);
                recyclerView.setLayoutManager(manager);
                editor.remove(KEY.STAGGERED_TWO);
                editor.apply();
            }
            break;

            case R.id.action_layout_staggered_two: {
                editor.putBoolean(KEY.STAGGERED_TWO, true);
                //boolean staggered = preferences.getBoolean(KEY.STAGGERED_TWO, false);
                Log.d(TAG, "onOptionsItemSelected: staggered two: ");
//                LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
//                        LinearLayoutManager.VERTICAL, false);
                StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(layoutManager);
                editor.remove(KEY.STAGGERED_ONE);
                editor.apply();
            }
            break;

            case android.R.id.home: {
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void queryData() {
        if (!videoList.isEmpty()) {
            videoList.clear();
        }

        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FavoriteActivity.this);
        Gson gson = new Gson();
        Map<String, ?> allEntries = preferences.getAll();

        if (!allEntries.isEmpty()) {
            try {

                for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                    Log.d(TAG, entry.getKey() + ": " + entry.getValue().toString());
                    if (entry.getKey().contains("star") || entry.getKey().contains("staggered")) {
                        Log.d(TAG, "onCreate: this key contains star");
                        continue;
                    }else {
                        String json = preferences.getString(entry.getKey(), "");
                        Video obj = gson.fromJson(json, Video.class);
                        videoList.add(obj);
                    }
                    //queryData(obj.getTitle_video());
                }

                Log.d(TAG, "onCreate: length video "+videoList.size());

                boolean staggered_one = preferences.getBoolean(KEY.STAGGERED_ONE, false);
                boolean staggered_two = preferences.getBoolean(KEY.STAGGERED_TWO, false);

                if (staggered_one) {
                    LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
                            LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(manager);

                }else if (staggered_two) {
                    StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
                    recyclerView.setLayoutManager(layoutManager);

                }else {
                    LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
                            LinearLayoutManager.VERTICAL, false);
                    recyclerView.setLayoutManager(manager);
                }

                if (videoList.isEmpty()) {
                    //Toast.makeText(getApplicationContext(), "empty list", Toast.LENGTH_LONG).show();
                    warningDialogEmpty();
                }else {
                    FavoriteAdapter adapter = new FavoriteAdapter(videoList, FavoriteActivity.this);
                    recyclerView.setAdapter(adapter);

                    adapter.notifyDataSetChanged();
                }
            }catch (Exception e) {
                Log.e(TAG, "onCreate: error "+e.getMessage());
            }
        }
    }

    private void warningDialogEmpty() {
        try {
            final Dialog dialog = new Dialog(FavoriteActivity.this);
            dialog.setContentView(R.layout.custem_dialog_empty);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            ImageView imageClose = (ImageView) dialog.findViewById(R.id.img_close);
            imageClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });


            ImageView imageDialog = (ImageView) dialog.findViewById(R.id.image_dialog_no_internet);
//            Picasso.get().load(R.drawable.no_internet)
//                    .centerInside()
//                    .fit()
//                    .placeholder(R.drawable.ic_account)
//                    .into(imageDialog);
            Glide.with(getApplicationContext()).load(R.drawable.empty)
                    .apply(new RequestOptions().centerInside())
                    .into(imageDialog);

            Button butTryAging = (Button) dialog.findViewById(R.id.but_try_aging);
            butTryAging.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    finish();
                }
            });

            dialog.show();
        }catch (Exception e) {
            Log.e(TAG, "warningDialogInternet: error: "+e.getMessage());
        }
    }

    @Override
    public void onRefresh() {
        try {
            queryData();
            refreshLayout.setRefreshing(false);
        }catch(Exception e) {

        }
    }

//    private void queryData(String title) {
//
//        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
//                LinearLayoutManager.VERTICAL, false);
//        recyclerView = findViewById(R.id.recycler_view_favorite);
//        recyclerView.setLayoutManager(manager);
//
//        Query query = FirebaseFirestore.getInstance()
//                .collection(KEY.VIDEOS)
//                //.orderBy(KEY.TIME_CREATED, Query.Direction.DESCENDING)
//                .whereEqualTo(KEY.TITLE_VIDEO, title);
//
//        query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    Log.e(TAG, "onEvent: Error "+e.getMessage());
//                    return;
//                }else{
//                    // Convert query snapshot to a list of chats
//                    List<Video> videos = queryDocumentSnapshots.toObjects(Video.class);
//                    Log.d(TAG, "onEvent: size "+videos.size());
//                }
//            }
//        });
//
//        FirestoreRecyclerOptions<Video> options = new FirestoreRecyclerOptions.Builder<Video>()
//                .setQuery(query, Video.class)
//                .build();
//
//        adapter = new FirestoreRecyclerAdapter<Video, helperHolder>(options) {
//
//
//            @NonNull
//            @Override
//            public helperHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
//                View view = null;
//                if (isAdmin) {
//                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_course, viewGroup, false);
//                }else {
//                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_course, viewGroup, false);
//                }
//
//                Log.d(TAG, "onCreateViewHolder: ");
//                return new helperHolder(view);
//            }
//
//            @Override
//            protected void onBindViewHolder(@NonNull helperHolder holder, final int position, @NonNull final Video model) {
//                Log.d(TAG, "onBindViewHolder: "+model.getTitle_video());
//                holder.bind(model);
//                holder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
//                        intent.putExtra(KEY.VIDEOS, model);
//                        intent.putExtra(KEY.POSITION, position);
//                        startActivity(intent);
//                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                    }
//                });
//
//                if (isAdmin) {
//                    holder.image_delete.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            //AlertDelete(model);
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onDataChanged() {
//                Log.d(TAG, "onDataChanged: ");
//                adapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onError(FirebaseFirestoreException e) {
//                // Called when there is an error getting a query snapshot. You may want to update
//                // your UI to display an error message to the user.
//                // ...
//                Log.e(TAG, "onError: "+e.getMessage());
//                if (FirebaseAuth.getInstance().getUid() != null) {
//                    //warningDialog();
//                }else {
//                    //warningDialogLogin();
//                }
//            }
//
////            @Override
////            protected void onBindViewHolder(@NonNull helperHolder holder, int position, @NonNull Courses model) {
////
////            }
//
//        };
//
//        query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
//                // Handle errors
//                if (e != null) {
//                    Log.w(TAG, "onEvent:error", e);
//                    return;
//                }
//
//                // Dispatch the event
//                if (queryDocumentSnapshots.isEmpty()) {
//                    if (FirebaseAuth.getInstance().getUid() != null) {
//                        //warningDialogPost();
//                    }else {
//                        //warningDialogLogin();
//                    }
//                }
////                for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
////                    // Snapshot of the changed document
////                    DocumentSnapshot snapshot = change.getDocument();
////
////                    switch (change.getType()) {
////                        case ADDED:
////                            // TODO: handle document added
////                            break;
////                        case MODIFIED:
////                            // TODO: handle document modified
////                            break;
////                        case REMOVED:
////                            // TODO: handle document removed
////                            break;
////                    }
////                }
//            }
//        });
//
//        recyclerView.setAdapter(adapter);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
////        FirebaseAuth.getInstance().addAuthStateListener(stateListener);
//        adapter.startListening();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
////        if (stateListener != null){
////            FirebaseAuth.getInstance().removeAuthStateListener(stateListener);
////        }
//        adapter.stopListening();
//    }
}

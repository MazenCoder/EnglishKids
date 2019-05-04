package com.mobidroid.englishkids;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.mobidroid.englishkids.adapter.helperHolder;
import com.mobidroid.englishkids.item.KEY;
import com.mobidroid.englishkids.item.User;
import com.mobidroid.englishkids.item.Video;

import java.util.List;


public class ListVideoActivity extends AppCompatActivity
        implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "ListVideoActivity";
    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter;
    //private FirestorePagingAdapter<Video, helperHolder> adapter;
    private ProgressBar progress;
    private SwipeRefreshLayout refreshLayout;
    private String TITLE;
    private Toolbar toolbar;
    private SharedPreferences preferences;// = PreferenceManager.getDefaultSharedPreferences(ListVideoActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video);

        FirebaseMessaging.getInstance().subscribeToTopic("TOPIC_ENGLISH_FUN");
        init();
        getUserDetails();
//        queryData("numbers");
        Intent intent = getIntent();
        if (intent != null) {
            TITLE = intent.getStringExtra(KEY.VIDEOS);
            Log.d(TAG, "onCreate: title: " + TITLE);
            queryData(TITLE);
        }
    }

    private void init() {


        recyclerView = findViewById(R.id.recycler_view_videos);
        preferences = PreferenceManager.getDefaultSharedPreferences(ListVideoActivity.this);
        progress = findViewById(R.id.progres_list);
        refreshLayout = findViewById(R.id.swipe_refresh_list_video);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(
                android.R.color.holo_red_light,
                R.color.black,
                R.color.yellow,
                android.R.color.holo_red_light);

        toolbar = findViewById(R.id.toolbar_list_video);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_back_home);
        upArrow.setColorFilter(getResources().getColor(R.color.White), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
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

    public void getUserDetails() {
        if (FirebaseAuth.getInstance().getUid() != null) {
            try {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                //but_admin = headerLayout.findViewById(R.id.fab_nav_header_admin);
                DocumentReference docRef = db.collection(KEY.USERS).document(FirebaseAuth.getInstance().getUid());
                docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            return;
                        } else {
                            try {
                                KEY.IS_ADMIN = documentSnapshot.toObject(User.class).isAdmin();
                            } catch (Exception e1) {
                                Log.e(TAG, "onEvent: error " + e1.getMessage());
                            }
                        }

                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "getUserDetails: error: " + e.getMessage());
            }
        }
    }


    private void queryData(String title) {

        progress.setVisibility(View.VISIBLE);
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

//        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
//                LinearLayoutManager.VERTICAL, false);
//        recyclerView = findViewById(R.id.recycler_view_videos);
//        recyclerView.setLayoutManager(manager);

        final Query query = FirebaseFirestore.getInstance()
                .collection(KEY.VIDEOS)
                .orderBy(KEY.TIME_CREATED, Query.Direction.DESCENDING)
                .whereEqualTo(KEY.TITLE_COURSE, title);

        query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.e(TAG, "onEvent: Error " + e.getMessage());
                    progress.setVisibility(View.INVISIBLE);
                    return;
                } else {
                    // Convert query snapshot to a list of chats
                    List<Video> videos = queryDocumentSnapshots.toObjects(Video.class);
                    Log.d(TAG, "onEvent: size " + videos.size());
                    if (queryDocumentSnapshots.getDocuments().isEmpty()) {
                        progress.setVisibility(View.INVISIBLE);
                        warningDialogIsEmpty();
                    } else if (!queryDocumentSnapshots.getDocuments().isEmpty()) {
                        progress.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        FirestoreRecyclerOptions<Video> options = new FirestoreRecyclerOptions.Builder<Video>()
                .setQuery(query, Video.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Video, helperHolder>(options) {


            @NonNull
            @Override
            public helperHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_video_list, viewGroup, false);
                Log.d(TAG, "onCreateViewHolder: ");
                return new helperHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final helperHolder holder, final int position, @NonNull final Video model) {
                Log.d(TAG, "onBindViewHolder: " + model.getTitle_video());
                //if (getItemCount())
                Log.d(TAG, "onBindViewHolder: item count: " + getItemCount());

                holder.bind(model);
                final String STAR = "star";
                String jsonHeart = preferences.getString(model.getTitle_video().trim(), "");
                //SharedPreferences.Editor prefsEditor = preferences.edit();
                if (!jsonHeart.isEmpty()) {
                    holder.likeHeart.setLiked(true);
                }

                String jsonStar = preferences.getString(model.getTitle_video().trim() + STAR, "");
                if (!jsonStar.isEmpty()) {
                    holder.likeStar.setLiked(true);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Save name video for reading
                        SharedPreferences.Editor prefsEditor = preferences.edit();
                        prefsEditor.putString(model.getTitle_video().trim() + STAR, model.getTitle_video().trim());
                        prefsEditor.apply();

                        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                        intent.putExtra(KEY.VIDEOS, model);
                        intent.putExtra(KEY.POSITION, position);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                });

                if (KEY.IS_ADMIN) {
                    holder.image_delete.setVisibility(View.VISIBLE);
                    holder.image_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDelete(model);
                        }
                    });

                }

                holder.likeHeart.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        try {
                            SharedPreferences.Editor prefsEditor = preferences.edit();
                            Gson gson = new Gson();
                            String json = gson.toJson(model);
                            prefsEditor.putString(model.getTitle_video().trim(), json);
                            prefsEditor.apply();
                            Log.d(TAG, "liked: ");
                        } catch (Exception e) {
                            Log.e(TAG, "liked: error " + e.getMessage());
                        }
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        try {
                            SharedPreferences.Editor prefsEditor = preferences.edit();
                            prefsEditor.remove(model.getTitle_video());
                            prefsEditor.apply();
                            Log.d(TAG, "unLiked: ");
                        } catch (Exception e) {
                            Log.e(TAG, "liked: error " + e.getMessage());
                        }
                    }
                });

            }

            @Override
            public void onDataChanged() {
                Log.d(TAG, "onDataChanged: ");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(FirebaseFirestoreException e) {
                // Called when there is an error getting a query snapshot. You may want to update
                // your UI to display an error message to the user.
                // ...
                Log.e(TAG, "onError: " + e.getMessage());
                progress.setVisibility(View.INVISIBLE);
                warningDialogInternet();
            }
        };

        recyclerView.setAdapter(adapter);
    }

    private void warningDialogIsEmpty() {
        try {
            final Dialog dialog = new Dialog(ListVideoActivity.this);
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
            Glide.with(getApplicationContext())
                    .load(R.drawable.empty)
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


    /**
     * private void queryData(String title) {
     * <p>
     * LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
     * LinearLayoutManager.VERTICAL, false);
     * recyclerView = findViewById(R.id.recycler_view_videos);
     * recyclerView.setLayoutManager(manager);
     * <p>
     * <p>
     * Query query = FirebaseFirestore.getInstance()
     * .collection(KEY.VIDEOS)
     * .orderBy(KEY.TIME_CREATED, Query.Direction.DESCENDING)
     * .whereEqualTo(KEY.TITLE_COURSE, title);
     * <p>
     * // This configuration comes from the Paging Support Library
     * // https://developer.android.com/reference/android/arch/paging/PagedList.Config.html
     * PagedList.Config config = new PagedList.Config.Builder()
     * .setEnablePlaceholders(false)
     * .setPrefetchDistance(10)
     * .setPageSize(20)
     * .build();
     * <p>
     * FirestorePagingOptions<Video> options = new FirestorePagingOptions.Builder<Video>()
     * .setLifecycleOwner(this)
     * .setQuery(query, config, Video.class)
     * .build();
     * <p>
     * <p>
     * adapter = new FirestorePagingAdapter<Video, helperHolder>(options) {
     *
     * @NonNull
     * @Override public helperHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
     * View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_course, viewGroup, false);
     * Log.d(TAG, "onCreateViewHolder: ");
     * return new helperHolder(view);
     * }
     * @Override protected void onBindViewHolder(@NonNull helperHolder holder, final int position, @NonNull final Video model) {
     * Log.d(TAG, "onBindViewHolder: "+model.getTitle_video());
     * //if (getItemCount())
     * Log.d(TAG, "onBindViewHolder: item count: "+getItemCount());
     * <p>
     * holder.bind(model);
     * final String STAR = "star";
     * final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ListVideoActivity.this);
     * String jsonHeart = preferences.getString(model.getTitle_video().trim(), "");
     * //SharedPreferences.Editor prefsEditor = preferences.edit();
     * if (!jsonHeart.isEmpty()) {
     * holder.likeHeart.setLiked(true);
     * }
     * <p>
     * String jsonStar = preferences.getString(model.getTitle_video().trim()+STAR, "");
     * if (!jsonStar.isEmpty()) {
     * holder.likeStar.setLiked(true);
     * }
     * <p>
     * holder.itemView.setOnClickListener(new View.OnClickListener() {
     * @Override public void onClick(View v) {
     * // Save name video for reading
     * SharedPreferences.Editor prefsEditor = preferences.edit();
     * prefsEditor.putString(model.getTitle_video().trim()+STAR, model.getTitle_video().trim());
     * prefsEditor.apply();
     * <p>
     * Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
     * intent.putExtra(KEY.VIDEOS, model);
     * intent.putExtra(KEY.POSITION, position);
     * startActivity(intent);
     * overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
     * }
     * <p>
     * });
     * <p>
     * if (KEY.IS_ADMIN) {
     * holder.image_delete.setVisibility(View.VISIBLE);
     * holder.image_delete.setOnClickListener(new View.OnClickListener() {
     * @Override public void onClick(View v) {
     * AlertDelete(model);
     * }
     * });
     * <p>
     * }
     * <p>
     * holder.likeHeart.setOnLikeListener(new OnLikeListener() {
     * @Override public void liked(LikeButton likeButton) {
     * try {
     * SharedPreferences.Editor prefsEditor = preferences.edit();
     * Gson gson = new Gson();
     * String json = gson.toJson(model);
     * prefsEditor.putString(model.getTitle_video().trim(), json);
     * prefsEditor.apply();
     * Log.d(TAG, "liked: ");
     * }catch (Exception e) {
     * Log.e(TAG, "liked: error "+e.getMessage());
     * }
     * }
     * @Override public void unLiked(LikeButton likeButton) {
     * try {
     * SharedPreferences.Editor prefsEditor = preferences.edit();
     * prefsEditor.remove(model.getTitle_video());
     * prefsEditor.apply();
     * Log.d(TAG, "unLiked: ");
     * }catch (Exception e) {
     * Log.e(TAG, "liked: error "+e.getMessage());
     * }
     * }
     * });
     * }
     * @Override public void startListening() {
     * adapter.notifyDataSetChanged();
     * super.startListening();
     * }
     * @Override protected void onLoadingStateChanged(@NonNull LoadingState state) {
     * switch (state) {
     * <p>
     * // initial load begun
     * case LOADING_INITIAL:
     * Log.d(TAG,"LOADING LOADING INITIAL");
     * if (progress != null && progress.getVisibility() == View.INVISIBLE) {
     * progress.setVisibility(View.VISIBLE);
     * }
     * break;
     * <p>
     * case LOADING_MORE:
     * //loading an additional page
     * Log.d("LOADING", "LOADING MORE");
     * if (progress != null && progress.getVisibility() == View.INVISIBLE) {
     * progress.setVisibility(View.VISIBLE);
     * }
     * break;
     * case LOADED:
     * //                        intReedsLogged = firestorePagingAdapter.getItemCount();
     * //                        reedsLogged.setText(String.valueOf(intReedsLogged));
     * if (progress != null && progress.getVisibility() == View.VISIBLE) {
     * progress.setVisibility(View.GONE);
     * }
     * // previous load (either initial or additional) completed
     * Log.d("LOADING", "LOADED");
     * break;
     * <p>
     * case FINISHED:
     * if (progress != null && progress.getVisibility() == View.VISIBLE) {
     * progress.setVisibility(View.GONE);
     * }
     * Log.d("LOADING","FINISHED");
     * <p>
     * break;
     * <p>
     * case ERROR:
     * //previous load (either initial or additional) failed.  Call the retry() method to retry load.
     * Log.d("LOADING", "LOADING error ");
     * Toast.makeText(getApplicationContext(), "loading error", Toast.LENGTH_LONG).show();
     * if (progress != null && progress.getVisibility() == View.VISIBLE) {
     * progress.setVisibility(View.GONE);
     * }
     * <p>
     * warningDialogInternet();
     * break;
     * <p>
     * }
     * }
     * };
     * <p>
     * query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
     * @Override public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
     * if (queryDocumentSnapshots.isEmpty()) {
     * warningDialogInternet();
     * }
     * }
     * });
     * <p>
     * recyclerView.setAdapter(adapter);
     * }
     */

    private void warningDialogInternet() {
        try {
            final Dialog dialog = new Dialog(ListVideoActivity.this);
            dialog.setContentView(R.layout.custem_dialog_internet);
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
            Glide.with(getApplicationContext())
                    .load(R.drawable.no_connection)
                    .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).centerInside())
                    .into(imageDialog);

            Button butTryAging = (Button) dialog.findViewById(R.id.but_try_aging);
            butTryAging.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recreate();
                    if (!TITLE.isEmpty()) {
                        queryData(TITLE);
                    }
                    dialog.dismiss();
                }
            });

            dialog.show();
        }catch (Exception e) {
            Log.e(TAG, "warningDialogInternet: error: "+e.getMessage());
        }
    }

    private void AlertDelete(final Video model) {
        try {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(ListVideoActivity.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(ListVideoActivity.this);
            }
            builder.setTitle(getString(R.string.app_name));
            builder.setMessage(getString(R.string.do_you_want_to_delete));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, int i) {
                    FirebaseStorage storage = FirebaseStorage.getInstance();
                    StorageReference photoRef = storage.getReferenceFromUrl(model.getUri_video());
                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            try {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference documentReference = db.collection(KEY.VIDEOS).document(model.getId());
                                documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d(TAG, "onComplete: isSuccessful: ");
                                            dialogInterface.dismiss();
                                        } else {
                                            Log.e(TAG, "onComplete: error: " + task.getException().getMessage());
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                Log.e(TAG, "onSuccess: error:" + e.getMessage());
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure: error: " + e.getMessage());
                        }
                    });
                }
            });

            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }catch (Exception e) {
            Log.e(TAG, "AlertDelete: error: "+e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //FirebaseAuth.getInstance().addAuthStateListener(stateListener);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (stateListener != null){
//            FirebaseAuth.getInstance().removeAuthStateListener(stateListener);
//        }
        adapter.stopListening();
    }

    @Override
    public void onRefresh() {
        try {
            if (!TITLE.isEmpty()) {
                recreate();
                queryData(TITLE);
            }
            refreshLayout.setRefreshing(false);
        }catch (Exception e) {

        }
    }
}

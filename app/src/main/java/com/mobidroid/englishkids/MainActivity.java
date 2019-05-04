package com.mobidroid.englishkids;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.arch.paging.PagedList;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.firebase.ui.firestore.paging.LoadingState;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mobidroid.englishkids.Service.FCMActivity;
import com.mobidroid.englishkids.adapter.ImageHolder;
import com.mobidroid.englishkids.adapter.SlideAdapter;
import com.mobidroid.englishkids.adapter.helperHolder;
import com.mobidroid.englishkids.item.Courses;
import com.mobidroid.englishkids.item.Images;
import com.mobidroid.englishkids.item.KEY;
import com.mobidroid.englishkids.item.User;
import com.mobidroid.englishkids.sing.SingInActivity;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

//    SwipeRefreshLayout.OnRefreshListener

    private static final String TAG = "MainActivity";
    private Toolbar toolbar;
    private SlideAdapter slideAdapter;
    private ViewPager viewPager;
    private CircleIndicator circleIndicator;
    private Handler handler;
    private Runnable runnable;
    private Timer timer;
    private FirebaseAuth.AuthStateListener stateListener;
    //private User user;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private View headerLayout;
    //private ProgressBar progressBar;
//    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private Images get_images;
    private List<String> uriList = new ArrayList<>();
    //    private FirestoreRecyclerAdapter<Courses, helperHolder> adapter;
    //private FirestoreRecyclerAdapter adapter;
    private FirestorePagingAdapter<Courses, helperHolder> adapter;

    private FirestoreRecyclerAdapter adapterImgs;
    private FloatingActionButton but_admin;
    private FirebaseFirestore db;
    private ProgressBar progressBar_home;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_drawer);

        FirebaseMessaging.getInstance().subscribeToTopic("TOPIC_ENGLISH_FUN");
        db = FirebaseFirestore.getInstance();


        init();
        getUserDetails();
        getImages();
        //getimage();
        setUpToolbar();
        //setUpSlideAdapter();
        queryData();
        setupFirebaseAuth();

    }

    private void init() {
        progressBar_home = findViewById(R.id.progress_main);
        navigationView   = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout     = (DrawerLayout) findViewById(R.id.drawer_layout);
//        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_home);
        recyclerView     = (RecyclerView) findViewById(R.id.recycler_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        headerLayout = navigationView.inflateHeaderView(R.layout.navigation_header);

//        FloatingActionButton but_admin = (FloatingActionButton) headerLayout.findViewById(R.id.fab_nav_header_admin);
//
//        but_admin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(getApplicationContext(), AdminActivity.class));
//                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//                    drawerLayout.closeDrawer(GravityCompat.START);
//                }
//            }
//        });
    }

    private void setUpToolbar() {
        toolbar = findViewById(R.id.toolbar_main_activity);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.action_account: {
                if (KEY.SIGN_IN) {
                    //Toast.makeText(getApplicationContext(), "welcome", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainActivity.this, AccountActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    startActivity(new Intent(MainActivity.this, SingInActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
            break;

            case R.id.action_favorite_page: {
                startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            break;

            case R.id.action_msg: {
                startActivity(new Intent(MainActivity.this, FCMActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            break;
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void getImages() {
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(KEY.IMAGES).document(KEY.IMAGES);

            docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    //images = documentSnapshot.toObject(Images.class).getImages_uri_map();

                    get_images = documentSnapshot.toObject(Images.class);

//                List<String> uris = new ArrayList<>();
                    if (get_images != null) {
                        String uri;
                        if (get_images.getImages_uri_map() != null && get_images.getImages_uri_map().size() != 0) {

                            Log.d(TAG, "getImages: "+get_images.getImages_uri_map().size());

                            Set<Map.Entry<String, String>> value = get_images.getImages_uri_map().entrySet();
                            for (Map.Entry<String, String> entry : value) {
                                Log.d(TAG, "bindAds: get key" + entry.getKey() + "  get Values" + entry.getValue());
                                uri = get_images.getImages_uri_map().get(entry.getKey());
                                uriList.add(uri);
                                Log.d(TAG, "onSuccess: "+uri);
                            }

                            Log.d(TAG, "onSuccess: uris: "+uriList.size());
                            setUpSlideAdapter(uriList);
                            //slideAdapter = new SlideAdapter(getApplicationContext(), uriList);
                            //viewPager.setAdapter(slideAdapter);
                            //circleIndicator.setViewPager(viewPager);
//                            Timer timer = new Timer();
//                            timer.scheduleAtFixedRate(new MyTimerTask(viewPagerAdapter.getCount()), 3000, 5000);
                        }
                    }
                }
            });
        }catch (Exception e) {
            Log.e(TAG, "getImages: error "+e.getMessage());
        }
    }

    public void getimage() {

        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        Query query = FirebaseFirestore.getInstance()
                .collection(KEY.IMAGES)
                //.document(KEY.IMAGES);
                .orderBy(KEY.TIME_CREATED, Query.Direction.DESCENDING);

//        FirestoreRecyclerOptions<Images> options = new FirestoreRecyclerOptions.Builder<Images>()
//                .setQuery(query, ImageHolder.class)
//                .build();

        FirestoreRecyclerOptions<Images> options = new FirestoreRecyclerOptions.Builder<Images>()
                .setQuery(query, Images.class)
                .build();


        adapterImgs = new FirestoreRecyclerAdapter<Images, ImageHolder>(options) {


            @NonNull
            @Override
            public ImageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_row, viewGroup, false);
                return new ImageHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ImageHolder holder, int position, @NonNull Images model) {
                holder.bind(model);

                String uri;
                if (model.getImages_uri_map() != null && model.getImages_uri_map().size() != 0) {

                    Log.d(TAG, "getImages: "+model.getImages_uri_map().size());

                    Set<Map.Entry<String, String>> value = model.getImages_uri_map().entrySet();
                    for (Map.Entry<String, String> entry : value) {
                        Log.d(TAG, "bindAds: get key" + entry.getKey() + "  get Values" + entry.getValue());
                        uri = model.getImages_uri_map().get(entry.getKey());
                        uriList.add(uri);
                        Log.d(TAG, "onSuccess: "+uri);
                    }

                    Log.d(TAG, "onSuccess: uris: "+uriList.size());
                    setUpSlideAdapter(uriList);
                    //slideAdapter = new SlideAdapter(getApplicationContext(), uriList);
                    //viewPager.setAdapter(slideAdapter);
                    //circleIndicator.setViewPager(viewPager);
//                            Timer timer = new Timer();
//                            timer.scheduleAtFixedRate(new MyTimerTask(viewPagerAdapter.getCount()), 3000, 5000);
                }
            }
        };
    }

    private void setUpSlideAdapter(List<String> uriList) {
        if (!uriList.isEmpty() || uriList.size() <= 0) {
            try {

                viewPager = findViewById(R.id.view_pager);
                slideAdapter = new SlideAdapter(this, uriList);
                viewPager.setAdapter(slideAdapter);

                circleIndicator = findViewById(R.id.circle_indicator);
                circleIndicator.setViewPager(viewPager);

                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {

                        int i = viewPager.getCurrentItem();
                        i++;
                        if (i == slideAdapter.uriList.size()) {
                            i = 0;
                        }
                        viewPager.setCurrentItem(i, true);
                    }
                };

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.post(runnable);
                    }
                }, 4000, 4000);

            }catch (Exception e) {
                Log.e(TAG, "setUpSlideAdapter: error: "+e.getMessage());
            }
        }

    }

//    @Override
//    public void onRefresh() {
//        try {
//            queryData();
//            swipeRefresh.setRefreshing(false);
//        }catch (Exception e) {
//            Log.e(TAG, "onRefresh error: "+e.getMessage());
//        }
//    }

    /**
    private void queryData() {
        try {
            LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
                    LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(manager);

            Query query = FirebaseFirestore.getInstance()
                    .collection(KEY.COURSES)
                    .orderBy(KEY.TIME_CREATED, Query.Direction.DESCENDING);

            query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.e(TAG, "onEvent: Error "+e.getMessage());
                        return;
                    }else{
                        // Convert query snapshot to a list of chats
                        List<Courses> courses = queryDocumentSnapshots.toObjects(Courses.class);
                        Log.d(TAG, "onEvent: size "+courses.size());

                        if (courses.size() == 0) {
                            AlertNoInternet();
                        }
                    }
                }
            });

            FirestoreRecyclerOptions<Courses> options = new FirestoreRecyclerOptions.Builder<Courses>()
                    .setQuery(query, Courses.class)
                    .build();

            adapter = new FirestoreRecyclerAdapter<Courses, helperHolder>(options) {


                @NonNull
                @Override
                public helperHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                    View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_course, viewGroup, false);
//                    if (isAdmin) {
//                        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_course, viewGroup, false);
//                    }else {
//                        view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_course, viewGroup, false);
//                    }
                    Log.d(TAG, "onCreateViewHolder: ");
                    return new helperHolder(view);
                }

                @Override
                protected void onBindViewHolder(@NonNull helperHolder holder, int position, @NonNull final Courses model) {
                    Log.d(TAG, "onBindViewHolder: "+model.getTitle());
                    holder.bind(model);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(getApplicationContext(), ListVideoActivity.class);
                            intent.putExtra(KEY.VIDEOS, model.getTitle());
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
                    Log.e(TAG, "onError: "+e.getMessage());
                    if (FirebaseAuth.getInstance().getUid() != null) {
                        //warningDialog();
                    }else {
                        //warningDialogLogin();
                    }
                }

//            @Override
//            protected void onBindViewHolder(@NonNull helperHolder holder, int position, @NonNull Courses model) {
//
//            }

            };

            query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    // Handle errors
                    if (e != null) {
                        Log.w(TAG, "onEvent:error", e);
                        return;
                    }

                    // Dispatch the event
                    if (queryDocumentSnapshots.isEmpty()) {
                        if (FirebaseAuth.getInstance().getUid() != null) {
                            //warningDialogPost();
                        }else {
                            //warningDialogLogin();
                        }
                    }
//                for (DocumentChange change : documentSnapshots.getDocumentChanges()) {
//                    // Snapshot of the changed document
//                    DocumentSnapshot snapshot = change.getDocument();
//
//                    switch (change.getType()) {
//                        case ADDED:
//                            // TODO: handle document added
//                            break;
//                        case MODIFIED:
//                            // TODO: handle document modified
//                            break;
//                        case REMOVED:
//                            // TODO: handle document removed
//                            break;
//                    }
//                }
                }
            });

            recyclerView.setAdapter(adapter);
        }catch (Exception e) {
            Log.e(TAG, "queryData: error "+e.getMessage());
        }
    }
     */
    private void queryData() {

        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);


        Query query = FirebaseFirestore.getInstance()
                .collection(KEY.COURSES)
                .orderBy(KEY.TIME_CREATED, Query.Direction.DESCENDING);

        // This configuration comes from the Paging Support Library
        // https://developer.android.com/reference/android/arch/paging/PagedList.Config.html
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Courses> options = new FirestorePagingOptions.Builder<Courses>()
                .setLifecycleOwner(this)
                .setQuery(query, config, Courses.class)
                .build();


        adapter = new FirestorePagingAdapter<Courses, helperHolder>(options) {

            @NonNull
            @Override
            public helperHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_course, viewGroup, false);
                Log.d(TAG, "onCreateViewHolder: ");
                return new helperHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull helperHolder holder, int position, @NonNull final Courses model) {
                Log.d(TAG, "onBindViewHolder: "+model.getTitle());
                holder.bind(model);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getApplicationContext(), ListVideoActivity.class);
                        intent.putExtra(KEY.VIDEOS, model.getTitle());
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
            }

            @Override
            public void startListening() {
                adapter.notifyDataSetChanged();
                super.startListening();
            }

            @Override
            protected void onLoadingStateChanged(@NonNull LoadingState state) {
                switch (state) {

                    // initial load begun
                    case LOADING_INITIAL:
                        Log.d(TAG,"LOADING LOADING INITIAL");
                        if (progressBar_home != null && progressBar_home.getVisibility() == View.INVISIBLE) {
                            progressBar_home.setVisibility(View.VISIBLE);
                        }
                        break;

                    case LOADING_MORE:
                        //loading an additional page
                        Log.d("LOADING", "LOADING MORE");
                        if (progressBar_home != null && progressBar_home.getVisibility() == View.INVISIBLE) {
                            progressBar_home.setVisibility(View.VISIBLE);
                        }
                        break;
                    case LOADED:
//                        intReedsLogged = firestorePagingAdapter.getItemCount();
//                        reedsLogged.setText(String.valueOf(intReedsLogged));
                        if (progressBar_home != null && progressBar_home.getVisibility() == View.VISIBLE) {
                            progressBar_home.setVisibility(View.GONE);
                        }
                        // previous load (either initial or additional) completed
                        Log.d("LOADING", "LOADED");
                        break;

                    case FINISHED:
                        if (progressBar_home != null && progressBar_home.getVisibility() == View.VISIBLE) {
                            progressBar_home.setVisibility(View.GONE);
                        }
                        Log.d("LOADING","FINISHED");

                        break;

                    case ERROR:
                        //previous load (either initial or additional) failed.  Call the retry() method to retry load.
                        Log.d("LOADING", "LOADING error ");
                        Toast.makeText(getApplicationContext(), "loading error", Toast.LENGTH_LONG).show();
                        if (progressBar_home != null && progressBar_home.getVisibility() == View.VISIBLE) {
                            progressBar_home.setVisibility(View.GONE);
                        }

                        warningDialogInternet();
                        break;

                }
            }
        };

        query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                if (queryDocumentSnapshots.isEmpty()) {
                    warningDialogInternet();
                }
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void warningDialogInternet() {
        final Dialog dialog = new Dialog(this);
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
//        Picasso.get().load(R.drawable.no_internet)
//                .centerInside()
//                .fit()
//                .placeholder(R.drawable.ic_account)
//                .into(imageDialog);
        Glide.with(getApplicationContext()).load(R.drawable.no_connection)
                .apply(new RequestOptions().placeholder(R.mipmap.ic_launcher).centerInside())
                .into(imageDialog);

        Button butTryAging = (Button) dialog.findViewById(R.id.but_try_aging);
        butTryAging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
                //queryData();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void AlertNoInternet() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
        }
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.do_you_want_to_exit));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {


            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void AlertDelete(final Courses model) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
        }
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.do_you_want_to_delete));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference documentReference = db.collection(KEY.COURSES).document(model.getId());
                documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: isSuccessful: ");
                            dialogInterface.dismiss();
                        }else{
                            Log.e(TAG, "onComplete: error: "+task.getException().getMessage());
                        }
                    }
                });

//                FirebaseStorage storage = FirebaseStorage.getInstance();
//                StorageReference photoRef = storage.getReferenceFromUrl(model.getUri_video());
//                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        try {
//
//                        }catch (Exception e) {
//                            Log.e(TAG, "onSuccess: error:"+e.getMessage());
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.e(TAG, "onFailure: error: "+e.getMessage());
//                    }
//                });
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: started.");

        stateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    KEY.SIGN_IN = true;
                    //check if email is verified
//                    if(user.isEmailVerified()){
//                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
//                        KEY.SIGN_IN = true;
//                    }else{
//                        KEY.SIGN_IN = false;
//                        FirebaseAuth.getInstance().signOut();
//                        Log.d(TAG, "onAuthStateChanged: user is not emailVerified");
//                    }

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: user is null");
                    KEY.SIGN_IN = false;
                }
                // ...
            }
        };
    }

    public void getUserDetails() {
        try {
            if (FirebaseAuth.getInstance().getUid() != null) {
                but_admin = headerLayout.findViewById(R.id.fab_nav_header_admin);
                DocumentReference docRef = db.collection(KEY.USERS).document(FirebaseAuth.getInstance().getUid());
                docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        if (e != null) {
                            return;
                        }else {
                            try {
                                KEY.IS_ADMIN = documentSnapshot.toObject(User.class).isAdmin();
                                if (KEY.IS_ADMIN) {
                                    but_admin.setVisibility(View.VISIBLE);
                                    but_admin.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startActivity(new Intent(getApplicationContext(), AdminActivity.class));
                                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                                        }
                                    });
                                } else {
                                    but_admin.setVisibility(View.INVISIBLE);
                                }
                            }catch (Exception e1) {
                                Log.e(TAG, "onEvent: error "+e1.getMessage());
                            }
                            }

                        }
                });

            }else {
                Log.e(TAG, "getUserDetails: user is null");
            }
        }catch (Exception e) {
            Log.e(TAG, "getDetailsUser: "+e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(stateListener);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (stateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(stateListener);
        }
        adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(MainActivity.this);
            }
            builder.setTitle(getString(R.string.app_name));
            builder.setMessage(getString(R.string.do_you_want_to_exit));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.finishAffinity(MainActivity.this);
                    finish();
                    dialogInterface.dismiss();
                }
            });

            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }
}

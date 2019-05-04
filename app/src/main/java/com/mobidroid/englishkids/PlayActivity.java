package com.mobidroid.englishkids;

import android.app.Application;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.mobidroid.englishkids.item.KEY;
import com.mobidroid.englishkids.item.Video;
import com.cloudinary.android.MediaManager;


import java.io.File;
import java.util.List;

import static com.mobidroid.englishkids.item.KEY.mStoragePermissions;

public class PlayActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "PlayActivity";
    private VideoView simpleVideoView;
    private MediaController mediaControls;
    private TextView tv_titleVideo, tv_desVideo;
    private ProgressBar progressBar;
    private Video video;
    private List<Video> videos;
    private int position;
    private int size;
    private Button butNext, butBack;
    private BottomNavigationView bottomNavigationView;
//    private BottomNavigationViewEx bnve;

    // TODO TEST CODE START
    public static class App extends Application  {

        private HttpProxyCacheServer proxy;

        public static HttpProxyCacheServer getProxy(Context context) {
            App app = (App) context.getApplicationContext();
            return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
        }

        private HttpProxyCacheServer newProxy() {
            return new HttpProxyCacheServer(this);
        }


    }
    private SimpleExoPlayer player;
    // TODO TEST CODE FENSH


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        FirebaseMessaging.getInstance().subscribeToTopic("TOPIC_ENGLISH_FUN");
        //MediaManager.init(this); // TODO TEST CODE

        mediaControls = new MediaController(this);

        init();

        Intent intent = getIntent();
        if (intent != null) {
            video = (Video) intent.getSerializableExtra(KEY.VIDEOS);
            position = intent.getIntExtra(KEY.POSITION, -1);
            Log.d(TAG, "onCreate: "+video.getLink_video());
            if (video != null) {
                //queryData(video);
                initializePlayer(video);
            }
        }

        queryData();

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (video != null) {
            savedInstanceState.putSerializable(KEY.VIDEOS, video);
            Log.d(TAG, "onRestoreInstanceState: save video ");
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState,
                                       PersistableBundle persistentState) {
        if (video != null) {
            savedInstanceState.putSerializable(KEY.VIDEOS, video);
            Log.d(TAG, "onRestoreInstanceState: save video ");
        }
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            // Change things
//            setContentView(R.layout.activity_play);
//            Log.d(TAG, "onConfigurationChanged: landscape");
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            // Change other things
//            Log.d(TAG, "onConfigurationChanged: portrait");
//            setContentView(R.layout.activity_play);
//        }
//    }

    private void init() {
        //simpleVideoView = findViewById(R.id.videoView);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.but_navigation_play);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

//        bnve                 = findViewById(R.id.bnve);
        tv_titleVideo        = findViewById(R.id.tv_title_video);
        tv_desVideo          = findViewById(R.id.tv_des_video);
        progressBar          = findViewById(R.id.progressBarPlay);
//        butNext         = findViewById(R.id.but_next_video);
//        butBack         = findViewById(R.id.but_back_video);

        //bnve.enableAnimation(false);
//        bnve.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
//                switch (menuItem.getItemId()) {
//                    case R.id.action_next: {
//                        nextVideo();
//                    }
//                    break;
//
//                    case R.id.action_download: {
//                        AlertDialogDownload();
//                    }
//                    break;
//
//                    case R.id.action_favorite: {
//                        if (video != null) {
//                            saveVideo(video);
//                        }
//                    }
//                    break;
//
//                    case R.id.action_back: {
//                        backVideo();
//                    }
//                    break;
//
//                }
//                return true;
//            }
//        });


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_next: {
                nextVideo();
            }
            break;

            case R.id.action_download: {
                if (mStoragePermissions) {
                    AlertDialogDownload();
                }else {
                    verifyStoragePermissions();
                }
            }
            break;

            case R.id.action_favorite: {
                if (video != null) {
                    saveVideo(video);
                }
            }
            break;

            case R.id.action_back: {
                backVideo();
            }
            break;

        }
        return true;
    }

    private void saveVideo(Video video) {
        Log.d(TAG, "saveVideo: ");
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String json = preferences.getString(video.getTitle_video().trim(), "");
            SharedPreferences.Editor prefsEditor = preferences.edit();
            if (json.isEmpty()) {
                Gson gson = new Gson();
                String json_save = gson.toJson(video);
                prefsEditor.putString(video.getTitle_video().trim(), json_save);
                prefsEditor.apply();
                Toast.makeText(getApplicationContext(), "success save obj", Toast.LENGTH_LONG).show();
            } else {
                prefsEditor.remove(video.getTitle_video());
                prefsEditor.apply();
                Toast.makeText(getApplicationContext(), "remove!", Toast.LENGTH_LONG).show();

            }
        }catch (Exception e) {
            Log.e(TAG, "saveVideo: error "+e.getMessage());
        }
    }


    private void AlertDialogDownload() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(PlayActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(PlayActivity.this);
        }
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.download_this_video));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                downloadManager(video.getUri_video());
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

    private void downloadManager(String url) {
        if (!url.isEmpty() && !video.getTitle_video().isEmpty()) {
            try {

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDescription("download");
                request.setTitle(""+video.getTitle_video());
                // in order for this if to run, you must use the android 3.2 to compile your app
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, ""+video.getTitle_video().trim()+".mp4");

                // get download service and enqueue file
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);

            }catch (Exception e) {
                Log.e(TAG, "downloadManager: error "+e.getMessage());
            }
        } else {
            Toast.makeText(getApplicationContext(), "error download video please try later!", Toast.LENGTH_LONG).show();
        }
    }

    public void verifyStoragePermissions() {
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this,
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,
                permissions[2]) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    this, permissions, KEY.REQUEST_CODE_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == KEY.REQUEST_CODE_IMAGE && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                mStoragePermissions = true;
            } else {
                mStoragePermissions = false;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void queryData(Video video) {
        try {

            if (mediaControls == null) {
                // create an object of media controller class
                mediaControls = new MediaController(this);
                mediaControls.setAnchorView(simpleVideoView);
            }
            // set the media controller for video view
            simpleVideoView.setMediaController(mediaControls);
            // set the uri for the video view
//        simpleVideoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.fishvideo));
            Log.d(TAG, "queryData: video url "+video.getUri_video());
            simpleVideoView.setVideoURI(Uri.parse(video.getUri_video()));
            // start a video
            simpleVideoView.start();
//            simpleVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    if (progressBar.getVisibility() == View.VISIBLE) {
//                        progressBar.setVisibility(View.VISIBLE);
//                    }
//                }
//            });

            simpleVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    if (MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START == what) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (MediaPlayer.MEDIA_INFO_BUFFERING_START == what) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    if (MediaPlayer.MEDIA_INFO_BUFFERING_END == what) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                    return false;
                }
            });

            // implement on completion listener on video view
            simpleVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Toast.makeText(getApplicationContext(), "Thank You...!!!", Toast.LENGTH_LONG).show(); // display a toast when an video is completed
                }
            });
            simpleVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Toast.makeText(getApplicationContext(), "Oops An Error Occur While Playing Video...!!!", Toast.LENGTH_LONG).show(); // display a toast when an error is occured while playing an video
                    return false;
                }
            });

            tv_titleVideo.setText(video.getTitle_video());
            tv_desVideo.setText(video.getDescription());

        }catch (Exception e) {
            Log.e(TAG, "queryData: error "+e.getMessage());
        }
    }

    private void nextVideo() {
        try {
            if (position != -1 && size != 0) {
                if (position+1 == size) {
                    //butNext.setEnabled(false);
                    Toast.makeText(PlayActivity.this, "this is a last video in this list", Toast.LENGTH_LONG).show();
                    AlertDialogCongratulations();
                    Log.d(TAG, "nextVideo: position = size ");
                }else {
                    Log.d(TAG, "nextVideo: position: " + position + " size: " + size);
                    //Log.d(TAG, "onEvent: size: "+videos.size());
                    Log.d(TAG, "onEvent: position: "+videos.get(position+1).getTitle_video());

                    Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                    intent.putExtra(KEY.VIDEOS, videos.get(position+1));
                    intent.putExtra(KEY.POSITION, position+1);
                    startActivity(intent);
                    finish();
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "nextVideo: error "+e.getMessage());
        }
    }

    private void backVideo() {
        try {
            if (position != -1 && size != 0) {
                if (position == 0) {
                    //butBack.setEnabled(false);
                    Toast.makeText(PlayActivity.this, "this is a first video in this list", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "nextVideo: position = size ");
                }else {
                    Log.d(TAG, "nextVideo: position: " + position + " size: " + size);
                    //Log.d(TAG, "onEvent: size: "+videos.size());
                    Log.d(TAG, "onEvent: position: "+videos.get(position-1).getTitle_video());

                    Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                    intent.putExtra(KEY.VIDEOS, videos.get(position-1));
                    intent.putExtra(KEY.POSITION, position-1);
                    startActivity(intent);
                    finish();
                }
            }
        }catch (Exception e) {
            Log.e(TAG, "nextVideo: error "+e.getMessage());
        }
    }

    private void AlertDialogCongratulations() {
        try {
            final Dialog dialog = new Dialog(PlayActivity.this);
            dialog.setContentView(R.layout.custem_dialog_congratulations);
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
            Glide.with(getApplicationContext()).load(R.drawable.congratulations)
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

    private void AlertDialogNextVideo() {
        try {
            final Dialog dialog = new Dialog(PlayActivity.this);
            dialog.setContentView(R.layout.custem_dialog_next_video);
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
            Glide.with(getApplicationContext()).load(R.drawable.wait)
                    .apply(new RequestOptions().centerInside())
                    .into(imageDialog);

            Button butTryAging = (Button) dialog.findViewById(R.id.but_try_aging);
            butTryAging.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    nextVideo();
                }
            });

            dialog.show();
        }catch (Exception e) {
            Log.e(TAG, "warningDialogInternet: error: "+e.getMessage());
        }
    }

    private void queryData() {
        try {
            Query query = FirebaseFirestore.getInstance()
                    .collection(KEY.VIDEOS)
                    .orderBy(KEY.TIME_CREATED, Query.Direction.DESCENDING)
                    .whereEqualTo(KEY.TITLE_COURSE, video.getTitle_course());

            query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.e(TAG, "onEvent: Error "+e.getMessage());
                        return;
                    }else{
                        // Convert query snapshot to a list of chats
                        videos = queryDocumentSnapshots.toObjects(Video.class);
                        size = videos.size();
                    }
                }
            });
        }catch (Exception e) {
            Log.e(TAG, "queryData: error "+e.getMessage());
        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        initializePlayer();
//    }

    private void initializePlayer(Video video) {
        // Create a default TrackSelector

        tv_titleVideo.setText(video.getTitle_video());
        tv_desVideo.setText(video.getDescription());

        // init button

//        if (position == 0) {
//            butBack.setEnabled(false);
//        }else {
//            butBack.setEnabled(true);
//        }
//
//        if (position+1 == size) {
//            butNext.setEnabled(false);
//        }else {
//            butNext.setEnabled(true);
//        }

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        //Initialize the player
        // = new SimpleExoPlayer();
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        //Initialize simpleExoPlayerView
        SimpleExoPlayerView simpleExoPlayerView = findViewById(R.id.exoplayer);
        simpleExoPlayerView.setPlayer(player);

        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                if (playbackState == ExoPlayer.STATE_BUFFERING){
//                    progressBar.setVisibility(View.VISIBLE);
//                } else {
//                    progressBar.setVisibility(View.INVISIBLE);
//                }
                switch (playbackState) {
                    case Player.STATE_ENDED:
                        Log.i("EventListenerState", "Playback ended!");
                        //Toast.makeText(PlayActivity.this, "end!", Toast.LENGTH_LONG).show();

                        if (position != -1 && size != 0) {
                            if (position + 1 == size) {
                                Toast.makeText(getApplicationContext(), "least video", Toast.LENGTH_LONG).show();
                                AlertDialogCongratulations();
                            }else {
                                //AlertDialogEndPlayVideo();
                                AlertDialogNextVideo();
                            }
                        }

                        player.setPlayWhenReady(false);
                        break;
                    case Player.STATE_READY:
                        Log.i("EventListenerState", "Playback State Ready!");
                        //hideProgressBar();
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                    case Player.STATE_BUFFERING:
                        Log.i("EventListenerState", "Playback buffering");
                        //showProgressBar();
                        progressBar.setVisibility(View.VISIBLE);

                        break;
                    case Player.STATE_IDLE:

                        break;

                }
            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });

        player.addVideoListener(new SimpleExoPlayer.VideoListener() {
            @Override
            public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

            }

            @Override
            public void onRenderedFirstFrame() {

            }
        });

        //simpleExoPlayerView.OnUnhandledKeyEventListener

//        simpleExoPlayerView.setOn

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)));

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // This is the MediaSource representing the media to be played.
        Uri videoUri = Uri.parse(video.getUri_video());
        MediaSource videoSource = new ExtractorMediaSource(videoUri,
                dataSourceFactory, extractorsFactory, null, null);

        // Prepare the player with the source.
        player.prepare(videoSource);
        player.setPlayWhenReady(true);

    }

    private void AlertDialogEndPlayVideo() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(PlayActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(PlayActivity.this);
        }
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.play_next_video));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                nextVideo();
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

    @Override
    public void onPause() {
        super.onPause();
        if (player!=null) {
            player.release();
            player = null;
        }
    }
}

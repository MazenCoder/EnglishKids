package com.mobidroid.englishkids.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobidroid.englishkids.R;
import com.mobidroid.englishkids.item.Courses;
import com.mobidroid.englishkids.item.KEY;
import com.mobidroid.englishkids.item.Video;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment {

    private static final String TAG = "VideoFragment";
    private Button but_video, but_upload;
    private AppCompatSpinner spinner_course;
    private EditText et_title, et_description, et_url;
    private String course = "";
    private String path_video = "";
    private String url_video_gallery = "";
    //private ArrayList<Integer> list_course = new ArrayList();
    private List<String> list_course = new ArrayList<String>();
    //  firebase
    private FirebaseFirestore db;// = FirebaseFirestore.getInstance();
    private DocumentReference documentReference;// = db.collection(KEY.VIDEOS).document();
    //private DocumentReference documentReference = db.collection(KEY.COURSES).document();

    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        db = FirebaseFirestore.getInstance();
        documentReference = db.collection(KEY.VIDEOS).document();
        Log.d(TAG, "VideoFragment: documentReference: "+documentReference.getId());
        getCourse(view);
        //setUpSpinner(view);
        init(view);
        return view;
    }

    private void getCourse(final View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(KEY.COURSES)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                Courses courses = document.toObject(Courses.class);
                                list_course.add(courses.getTitle());
                            }
                            //  call spinner
                            setUpSpinner(view);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void setUpSpinner(View view) {
        try {
            spinner_course = (AppCompatSpinner) view.findViewById(R.id.spinner_course);
            // Creating adapter for spinner
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
                    getContext(), android.R.layout.simple_spinner_item, list_course);

            spinner_course.setAdapter(dataAdapter);

            spinner_course.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (parent.getItemAtPosition(position) != null) {
                        course = parent.getItemAtPosition(position).toString().trim();
                    }
                    Log.d(TAG, "onItemSelected: item: "+course);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }catch (Exception e) {
            Log.e(TAG, "setUpSpinner: error "+e.getMessage());
        }
    }

    private void init(View view) {
        et_title       = view.findViewById(R.id.et_title_video);
        et_description = view.findViewById(R.id.et_description_video);
        et_url         = view.findViewById(R.id.et_url_video);
        but_video      = view.findViewById(R.id.but_get_video);
        but_upload     = view.findViewById(R.id.but_upload_video);

        but_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getVideo();
            }
        });

        but_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url_video_link = et_url.getText().toString().trim();
                if (!course.isEmpty()) {

                    if (!et_title.getText().toString().isEmpty()) {

                        if (!et_description.getText().toString().isEmpty()) {

                            if (url_video_link.isEmpty() && url_video_gallery.isEmpty()) {
                                Toast.makeText(getActivity(), "you need to choose video", Toast.LENGTH_LONG).show();
                                return;
                            }else if (!url_video_gallery.isEmpty()) {
                                uploadVideo(url_video_gallery);
                            }else {
                                uploadVideoLink(url_video_link);
                            }

                        }else {
                            Toast.makeText(getActivity(), "please text same text description course", Toast.LENGTH_LONG).show();
                        }

                    }else {
                        Toast.makeText(getActivity(), "please enter title video", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getActivity(), "please select course", Toast.LENGTH_LONG).show();
                }



            }
        });
    }

    private void uploadVideoLink(String link) {
        if (!link.isEmpty()) {
            try {
                Video video = new Video();
                video.setTitle_course(course);
                video.setTitle_video(et_title.getText().toString());
                video.setDescription(et_description.getText().toString());
                video.setLink_video(link);
                video.setId(documentReference.getId());

                documentReference
                        .set(video, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        et_title.setText("");
                        et_description.setText("");
                        et_url.setText("");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: error "+e.getMessage());
                    }
                });
            }catch (Exception e) {
                Log.e(TAG, "uploadVideoLink: error "+e.getMessage());
            }
        }else {
            Toast.makeText(getActivity(), "empty link eo not correct", Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void getVideo() {
        Intent intent = new Intent();
        intent.setType(KEY.VIDEO_TYPE);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"), KEY.REQUEST_GALLERY_VIDEO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == KEY.REQUEST_GALLERY_VIDEO) {
                Uri selecteUri = data.getData();
                Log.d(TAG, "onActivityResult: path "+selecteUri);
                // OI FILE Manager
                path_video = selecteUri.getPath();
                url_video_gallery  =  selecteUri.toString();
                Log.d(TAG, "onActivityResult: path "+path_video);
            }
        }
    }

    private void uploadVideo(String video_uri) {
        try {
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child(getString(R.string.app_name))
                    .child(KEY.VIDEOS)
                    .child(et_title.getText().toString().toLowerCase().trim());

            UploadTask uploadTask = storageReference.putFile(Uri.parse(video_uri));

            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
//                        Check.hideDialog(progress);
                        Log.d(TAG, "onComplete: is successful");
                        //Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.upload_image));
                    }else {
                        //Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.failed));
                        //Check.hideDialog(progress);
                    }
                }
            });

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        //Check.hideDialog(progress);
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        Video video = new Video();
                        video.setTitle_course(course);
                        video.setTitle_video(et_title.getText().toString());
                        video.setDescription(et_description.getText().toString());
                        video.setUri_video(downloadUri.toString());
                        video.setId(documentReference.getId());

                        //uri_image.put("uri_img_"+i, downloadUri.toString());
                        //Images images = new Images();
                        //images.setImages_uri_map(uri_image);
//                        Courses courses = new Courses();
//                        courses.setId(documentReference.getId());
//                        courses.setImage(downloadUri.toString());
//                        courses.setTitle(tie_title.getText().toString().trim());
//                        courses.setDescription(null);
                        documentReference
//                                .collection(KEY.IMAGES)
//                                .document(documentReference.getId())
                                //documentReference
                                .set(video, SetOptions.merge())
                                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: Updated FireStore");
                                        et_description.setText("");
                                        et_title.setText("");
//                                        Check.hideDialog(progress);
                                        Log.d(TAG, "onSuccess: "+documentReference.getId());
//                                        for (String token : tokensAdmin) {
//                                            sendMessage(getString(R.string.new_ad_has_been_added),
//                                                    getString(R.string.by)+" "+advertise.getName(),
//                                                    token);
//                                        }
                                        //getImages();
                                        //queryData();
                                    }
                                }).addOnFailureListener(getActivity(), new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//                                Check.hideDialog(progress);
                                Log.e(TAG, "onFailure: "+e.getMessage());
                            }
                        });
                    } else {
                        // Handle failures
                        // ...
//                        Check.hideDialog(progress);
                    }
                }
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("Upload is " + progress + "% done");
                    Toast.makeText(getActivity(), getActivity().getString(R.string.upload_is)+" "+ Integer.valueOf((int) progress) + "% "+getString(R.string.done), Toast.LENGTH_SHORT).show();
                }
            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e(TAG, "onPaused: upload paused");
                    //Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.upload_paused));
                    Toast.makeText(getActivity(), "upload paused", Toast.LENGTH_LONG).show();
//                    Check.hideDialog(progress);
                }
            });
        }catch (Exception e) {
            Log.e(TAG, "uploadVideo: error "+e.getMessage());
        }
    }
}

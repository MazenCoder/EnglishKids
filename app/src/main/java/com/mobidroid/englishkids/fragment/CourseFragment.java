package com.mobidroid.englishkids.fragment;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobidroid.englishkids.R;
import com.mobidroid.englishkids.adapter.helperHolder;
import com.mobidroid.englishkids.item.Courses;
import com.mobidroid.englishkids.item.KEY;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;
import static com.mobidroid.englishkids.item.KEY.mStoragePermissions;

/**
 * A simple {@link Fragment} subclass.
 */
public class CourseFragment extends Fragment {

    private static final String TAG = "CourseFragment";
    private Button but_chooseImage;
    private TextInputEditText tie_title, tie_des;
    private TextInputLayout til_title, til_des;
    private Toolbar toolbar;

    //  firebase
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notesCollectionRef;
    private DocumentReference documentReference = db
            .collection(KEY.COURSES)
            .document();
    private RecyclerView recyclerView;

    private FirestoreRecyclerAdapter<Courses, helperHolder> adapter;

    public CourseFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_course, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        toolbar      = view.findViewById(R.id.toolbar_fragment_course);
        recyclerView = view.findViewById(R.id.recycler_view_course);
        tie_title    = view.findViewById(R.id.tie_title_course_admin);
        til_title    = view.findViewById(R.id.til_title_course_admin);

        tie_des      = view.findViewById(R.id.tie_des_course_admin);
        til_des      = view.findViewById(R.id.til_des_course_admin);

        but_chooseImage = view.findViewById(R.id.but_choose_image);
        but_chooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!tie_title.getText().toString().isEmpty()) {
                    til_title.setErrorEnabled(false);

                    if (!tie_des.getText().toString().isEmpty()) {
                        til_des.setErrorEnabled(false);
                        chooseImages();
                    }else{
                        til_des.setError(getString(R.string.enter_des_course));
                    }
                }else{
                    til_title.setError(getString(R.string.enter_title_course));
                }
            }
        });

        queryData();
    }

    private void chooseImages() {
        if(mStoragePermissions){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent,getString(R.string.select_image)), KEY.REQUEST_CODE_COURSE);
        }else{
            verifyStoragePermissions();
        }
    }

    public void verifyStoragePermissions() {
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getActivity(),
                permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getActivity(),
                permissions[2] ) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    getActivity(), permissions, KEY.REQUEST_CODE_COURSE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == KEY.REQUEST_CODE_COURSE && grantResults.length > 0) {
            //if (grantResults[0] == PackageManager.PERMISSION_GRANTED)

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == KEY.REQUEST_CODE_COURSE) {
            if (data.getData() != null) {
                Uri uri_image = data.getData();
                if (uri_image != null) {
                    uploadImage(uri_image);
                }
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                            this.getActivity().getContentResolver() , Uri.parse(uri_image.toString()));
                    Bitmap bitmap_result = rotateImageIfRequired(bitmap, uri_image);
                    //uploadImage(bitmap_result);
                    //uploadCourse(bitmap_result);
                }catch (Exception e) {
                    Log.e(TAG, "onActivityResult: error "+e.getMessage());
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = getActivity().getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private void uploadImage(Uri image_uri) {
        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap_result.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            byte[] data = baos.toByteArray();

            final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child(getString(R.string.app_name))
                    .child(KEY.COURSES)
//                    .child(documentReference.getId())
//                    .child(KEY.IMAGES)
                    .child(tie_title.getText().toString().trim());

            //UploadTask uploadTask = storageReference.putBytes(data);
            UploadTask uploadTask = storageReference.putFile(image_uri);

            uploadTask.addOnCompleteListener(getActivity(), new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
//                        Check.hideDialog(progress);
                        Log.d(TAG, "onComplete: is successful");
                        //Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.upload_image));
                        Toast.makeText(getActivity(), "success upload image", Toast.LENGTH_LONG).show();
                    }else {
                        //Check.MessageSnackBarShort(findViewById(R.id.coordinatorLayout_add_course), getString(R.string.failed));
                        Toast.makeText(getActivity(), "field upload image", Toast.LENGTH_LONG).show();
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

                        //uri_image.put("uri_img_"+i, downloadUri.toString());
                        //Images images = new Images();
                        //images.setImages_uri_map(uri_image);
                        Courses courses = new Courses();
                        courses.setId(documentReference.getId());
                        courses.setImage(downloadUri.toString());
                        courses.setTitle(tie_title.getText().toString().trim());
                        courses.setDescription(tie_des.getText().toString().trim());
                        courses.setTime_created(null);
                        documentReference
//                                .collection(KEY.IMAGES)
//                                .document(documentReference.getId())
                                //documentReference
                                .set(courses, SetOptions.merge())
                                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "onSuccess: Updated FireStore");
                                        tie_title.setText("");
                                        tie_des.setText("");
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
                    Toast.makeText(getActivity(), "upload is: "+ Integer.valueOf((int) progress) + "% done", Toast.LENGTH_SHORT).show();
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
            Log.e(TAG, "uploadImage: error "+e.getMessage());
        }
    }

    private void queryData() {

        LinearLayoutManager manager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        Query query = FirebaseFirestore.getInstance()
                .collection(KEY.COURSES)
                .orderBy(KEY.TIME_CREATED, Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Courses> options = new FirestoreRecyclerOptions.Builder<Courses>()
                .setQuery(query, Courses.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Courses, helperHolder>(options) {

            @NonNull
            @Override
            public helperHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.custom_course, viewGroup, false);
                return new helperHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull helperHolder holder, int position, @NonNull Courses model) {
                holder.bind(model);
            }

            @Override
            public void onDataChanged() {
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



        };

        query.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
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
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}

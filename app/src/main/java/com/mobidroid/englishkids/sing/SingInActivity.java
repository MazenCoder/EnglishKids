package com.mobidroid.englishkids.sing;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;


import com.mobidroid.englishkids.MainActivity;
import com.mobidroid.englishkids.R;
import com.mobidroid.englishkids.fragment.PasswordResetDialog;
import com.mobidroid.englishkids.fragment.ResendVerificationDialog;
import com.mobidroid.englishkids.item.Check;
import com.mobidroid.englishkids.item.KEY;
import com.mobidroid.englishkids.item.User;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import javax.annotation.Nullable;

public class SingInActivity extends AppCompatActivity {

    private FirebaseAuth.AuthStateListener stateListener;
    private ProgressBar progressBar;
    private TextInputLayout til_log_email, til_log_pass;
    private EditText etLogEmail, etLogPass;
    private static final String TAG = "SingInActivity";
    private User user_account;// = new User();
    private String messaging_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_in);

        //user_account = new User();
        setupFirebaseAuth();
        setUpView();
        getDetailsUser();

        Intent intent = getIntent();
        if (intent.hasExtra(KEY.USERS)) {

            try {
                user_account = (User) intent.getSerializableExtra(KEY.USERS);
                if (user_account != null) {
                    etLogEmail.setText(user_account.getEmail());
                    etLogPass.setText(user_account.getPassword());

                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                    DocumentReference documentReference = firestore.collection(KEY.USERS)
                            .document(user_account.getUid());
//                documentReference.set(user)
                    documentReference
                            .set(user_account, SetOptions.merge())
                            .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Welcome " + user_account.getName(), Toast.LENGTH_LONG).show();

                                    setupFirebaseAuth();

                                }
                            }).addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "onFailure: "+e.getMessage());
                        }
                    });

                    Log.d(TAG, "onCreate: FireStore Insert new User");
                }
            }catch (Exception e) {
                Log.e(TAG, "onCreate: error: "+e.getMessage());
            }

        }


        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Log.e(TAG, "Token is: "+instanceIdResult.getToken());
                        messaging_token = instanceIdResult.getToken();
                    }
                });

    }

    public void getDetailsUser() {
        try {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection(KEY.USERS)
                    .document(FirebaseAuth.getInstance().getUid());
            docRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                    if (e != null) {
                        return;
                    }else {
                        user_account = documentSnapshot.toObject(User.class);
                        if (user_account != null) {
                            messaging_token = user_account.getMessaging_token();
                        }

                    }

                }
            });
        }catch (Exception e) {
            Log.e(TAG, "getDetailsUser: "+e.getMessage());
        }
    }


    private void setUpView() {

        progressBar     = (ProgressBar)findViewById(R.id.progress);

        // widget
        til_log_email   = (TextInputLayout) findViewById(R.id.til_log_email);
        til_log_pass    = (TextInputLayout) findViewById(R.id.til_log_pas);

        etLogEmail      = (EditText) findViewById(R.id.et_lg_email);
        etLogPass       = (EditText) findViewById(R.id.et_lg_pass);
    }

    private void showDialog(){
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideDialog(){
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void login(View view) {

        if (Check.isNotEmpty(etLogEmail)) {

            if (Check.isValidEmail(etLogEmail.getText().toString().toLowerCase().trim())) {
                til_log_email.setErrorEnabled(false);

                if (Check.isNotEmpty(etLogPass)) {

                    if (Check.isValidPassword(etLogPass.getText().toString().trim())) {
                        til_log_pass.setErrorEnabled(false);

                        // Login User
                        signInUser(etLogEmail.getText().toString().toLowerCase().trim(),
                                etLogPass.getText().toString().trim());
                    }else {
                        til_log_pass.setError(getString(R.string.password_incorrect));
                    }
                }else {
                    til_log_pass.setError(getString(R.string.enter_your_pass));
                }
            }else {
                til_log_email.setError(getString(R.string.email_incorrect));
            }
        }else {
            til_log_email.setError(getString(R.string.enter_your_email));
        }
    }

    private void signInUser(String email, String pass) {

        showDialog();
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        updateToken();

//                        startActivity(new Intent(getApplicationContext(), Home.class));
//                        Toast.makeText(getApplicationContext(), "Welcome", Toast.LENGTH_LONG).show();
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Check.ToastMessage(getApplicationContext(), getString(R.string.email_password_incorrect));
                hideDialog();
            }
        });
    }

    private void updateToken() {

            try {
                if ((!TextUtils.isEmpty(messaging_token)) && (!messaging_token.equals(user_account.getMessaging_token()))) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(KEY.MSG_TOKEN, messaging_token);
                db.collection(KEY.USERS).document(FirebaseAuth.getInstance().getUid())
                        .update(hashMap).addOnSuccessListener(SingInActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideDialog();
                    }
                }).addOnFailureListener(SingInActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.e(TAG, "onFailure: "+e.getMessage());
                    }
                });

        }
        }catch (Exception e) {
        Log.e(TAG, "updateCity: "+e.getMessage());
    }
    }

    /*
        ----------------------------- Firebase setup ---------------------------------
     */
    private void setupFirebaseAuth() {
        try {
            Log.d(TAG, "setupFirebaseAuth: started.");

            stateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    final FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {

                        //User user_auth = new User();
                        if (user_account != null) {

                            if (!user_account.getUid().isEmpty()) {

                                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                                DocumentReference documentReference = firestore.collection(KEY.USERS)
                                        .document(user_account.getUid());
                                documentReference
                                        .set(user_account, SetOptions.merge())
                                        .addOnSuccessListener(SingInActivity.this, new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Welcome " + user_account.getName(), Toast.LENGTH_LONG).show();

                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                KEY.SIGN_IN = true;
                                                startActivity(intent);
                                                finish();

                                            }
                                        }).addOnFailureListener(SingInActivity.this, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        Log.e(TAG, "onFailure: "+e.getMessage());
                                    }
                                });
                            }
                            //user_auth = user_account;

                        } else {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            KEY.SIGN_IN = true;
                            startActivity(intent);
                            finish();
                        }

//                    else if (user != null) {
//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        KEY.SIGN_IN = true;
//                        startActivity(intent);
//                        finish();
//                    }





                        //check if email is verified
//                    if(user.isEmailVerified()){
//                        Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
////                        Toast.makeText(getApplicationContext(), getString(R.string.authentication_with) + user.getEmail(), Toast.LENGTH_LONG).show();
//
//                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        KEY.SIGN_IN = true;
//                        startActivity(intent);
//                        finish();
//
//                    }else{
//                        Toast.makeText(getApplicationContext(), getString(R.string.inbox_verification), Toast.LENGTH_LONG).show();
//                        KEY.SIGN_IN = false;
//                        FirebaseAuth.getInstance().signOut();
//                        Log.d(TAG, "onAuthStateChanged: user is not emailVerified");
//                    }

                    } else {
                        // User is signed out
                        Log.d(TAG, "onAuthStateChanged: user is null");
                    }
                    // ...
                }
            };
        }catch (Exception e) {
            Log.e(TAG, "setupFirebaseAuth: error "+e.getMessage());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(stateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (stateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(stateListener);
        }
    }

    public void goToSignUp(View view) {
        startActivity(new Intent(SingInActivity.this, SingUpActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void forgetPass(View view) {
        PasswordResetDialog dialog = new PasswordResetDialog();
        dialog.show(getSupportFragmentManager(), "dialog_password_reset");
    }

    public void resendVerEmail(View view) {
        ResendVerificationDialog dialog = new ResendVerificationDialog();
        dialog.show(getSupportFragmentManager(), "dialog_resend_email_verification");
    }
}

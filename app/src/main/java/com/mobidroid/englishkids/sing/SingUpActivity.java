package com.mobidroid.englishkids.sing;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.mobidroid.englishkids.R;
import com.mobidroid.englishkids.item.Check;
import com.mobidroid.englishkids.item.KEY;
import com.mobidroid.englishkids.item.User;


public class SingUpActivity extends AppCompatActivity {

    private static final String TAG = "SingUpActivity";
    private ProgressBar progressBar;

    private EditText nameUser, emailUser, passUser;//, phoneUser;
    private TextInputLayout til_name, til_email, til_pass;//, til_phone;
    private String messaging_token;
    // Firebase
    private FirebaseFirestore db;
    private DocumentReference documentRef;

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(LocalHelper.onActivity(newBase));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        db = FirebaseFirestore.getInstance();
        documentRef = db.collection(KEY.USERS).document();
        setUpView();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        messaging_token = instanceIdResult.getToken();
                        Log.d("Token: ",instanceIdResult.getToken());

                    }
                });
    }

    private void setUpView() {

        progressBar     = (ProgressBar)findViewById(R.id.progress_account);

        nameUser        = (EditText)findViewById(R.id.et_sig_name);
        emailUser       = (EditText)findViewById(R.id.et_sig_email);
        passUser        = (EditText)findViewById(R.id.et_sig_pass);

        til_name        = (TextInputLayout)findViewById(R.id.til_name);
        til_email       = (TextInputLayout)findViewById(R.id.til_email);
        til_pass        = (TextInputLayout)findViewById(R.id.til_pass);
    }

    public void RegisterUser(View view) {

        if (Check.isNotEmpty(nameUser)) {
            til_name.setErrorEnabled(false);

            if (Check.isNotEmpty(emailUser)) {
//                    til_email.setErrorEnabled(false);

                if (Check.isValidEmail(emailUser.getText().toString().toLowerCase().trim())) {
                    til_email.setErrorEnabled(false);

                    if (Check.isNotEmpty(passUser)) {

                        if (Check.isValidPassword(passUser.getText().toString().trim())) {
                            til_pass.setErrorEnabled(false);

                            registerNewEmail(emailUser.getText().toString().toLowerCase().trim(),
                                    passUser.getText().toString().trim());
                        }else {
                            til_pass.setError(getString(R.string.password_incorrect));
                        }
                    }else {
                        til_pass.setError(getString(R.string.enter_your_pass));
                    }
                }else {
                    til_email.setError(getString(R.string.email_incorrect));
                }
            }else til_email.setError(getString(R.string.enter_your_email));
        }else {
            til_name.setError(getString(R.string.enter_your_name));
        }
    }

    private void registerNewEmail(final String email, final String pass) {

//        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//        if (result.isSuccess()) {
//            // Google Sign In was successful, authenticate with Firebase
//            GoogleSignInAccount account = result.getSignInAccount();
//            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
//
//            mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                @Override
//                public void onComplete(@NonNull Task<AuthResult> task) {
//                    if(!task.isSuccessful()){
//                        //Control reaches here and prints the exception as stated above
//                        Log.d(SyncStateContract.Constants.TAG, "onError:: Code : "+ task.getException().toString());
//                    }
//
//                }
//            });
//        }




        showDialog();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        final User user = new User();

                        user.setUid(authResult.getUser().getUid());
                        //user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        Log.d(TAG, "UserId: "+FirebaseAuth.getInstance().getCurrentUser().getUid());
                        Log.d(TAG, "UserId: "+authResult.getUser().getUid());

                        user.setName(nameUser.getText().toString());
                        user.setEmail(email);
                        user.setPassword(pass);
                        user.setTime_created(null);
                        user.setAdmin(false);

                        //  SET MESSAGING TOKEN ID
                        user.setMessaging_token(messaging_token);

                        //  VERIFICATION EMAIL

                        setUserDetails(nameUser.getText().toString());


                        Intent i = new Intent(getApplicationContext(), SingInActivity.class);
                        i.putExtra(KEY.USERS, user);
                        FirebaseAuth.getInstance().signOut();
                        startActivity(i);
                        finish();
                    }
                })

                /**
                .addOnCompleteListener(
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            final User user = new User();

                            user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            Log.d(TAG, "UserId: "+FirebaseAuth.getInstance().getCurrentUser().getUid());

                            user.setName(nameUser.getText().toString());
                            user.setEmail(email);
                            user.setPassword(pass);
//                            user.setPhone(phoneUser.getText().toString().trim());
//                            user.setCity(spinner_city);
                            user.setTime_created(null);
                            user.setAdmin(false);

                            //  SET MESSAGING TOKEN ID
                            user.setMessaging_token(messaging_token);

                            //  VERIFICATION EMAIL
//                            sendVerificationEmail();

                            setUserDetails(nameUser.getText().toString());


//                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//                            DocumentReference documentReference = firestore.collection(KEY.USERS)
//                                    .document(FirebaseAuth.getInstance().getUid());
//                            documentReference.set(user).addOnCompleteListener(SingUpActivity.this, new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//
//                                    if (task.isSuccessful()) {
//                                        Log.d(TAG, "onComplete: successful");
//                                    }else {
//                                        Log.e(TAG, "onComplete: "+task.getException().getMessage());
//                                    }
//                                }
//                            });


                            Intent i = new Intent(getApplicationContext(), SingInActivity.class);
//                            i.putExtra(Check.KEY_EMAIL, email);
//                            i.putExtra(Check.KEY_PASS, pass);
                            i.putExtra(KEY.USERS, user);
                            FirebaseAuth.getInstance().signOut();
                            startActivity(i);
                            ///setResult(RESULT_OK, i);
                            finish();
                        } else {
                            Log.e(TAG, "onComplete: error: "+task.getException().getMessage());
                        }
                        hideDialog();
                    }
                }
        )
                */
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: "+e.getMessage());
                Check.ToastMessage(getApplicationContext(), getString(R.string.the_email_is_already_in_use));
                hideDialog();
            }
        });

    }

//    private void serUserFiretor(String name, String email, String pass, String phone, String spinner_city) {
//        Log.d(TAG, "serUserFiretor: "+name+"\n"+email+"\n"+pass+"\n"+phone);
//
//        User user = new User();
//        user.setName(name);
//        user.setEmail(email);
//        user.setPassword(pass);
//        user.setPhone(phone);
//        user.setCity(spinner_city);
//        user.setTime_created(null);
//        documentRef.set(user);
//    }



//    private void sendVerificationEmail() {
//        Log.d(TAG, "sendVerificationEmail: ");
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if (task.isSuccessful()) {
//
//                        Log.d(TAG, "onComplete: sendVerificationEmail");
//                        Check.ToastMessage(getApplicationContext(), getString(R.string.check_your_email_link));
//                    }
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//
//                    Log.e(TAG, "onFailure: sendVerificationEmail");
//                    Toast.makeText(getApplicationContext(), R.string.could_not_sent_verification + "\n"
//                            + e.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            });
//        }
//    }

    private void setUserDetails(String name){
        Log.d(TAG, "setUserDetails: setUserDetails");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
//                    .setPhotoUri()
            user.updateProfile(profileChangeRequest)
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: setUserDetails");
//                            Toast.makeText(getApplicationContext(), "User Details", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "OnFailure setUserDetails");
                }
            });

        }
    }

    private void serUserFiretor(String name, String email, String pass, String phone, String city) {

        Log.d(TAG, "serUserFiretor: ");
        Toast.makeText(getApplicationContext(),name+"\n"+email+"\n"+pass+"\n"+phone, Toast.LENGTH_LONG).show();

        User user = new User();

        user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
        Log.d(TAG, "UserId: "+FirebaseAuth.getInstance().getCurrentUser().getUid());

        user.setName(name);
        user.setEmail(email);
        user.setPassword(pass);
//        user.setPhone(phone);
//        user.setCity(city);
        user.setTime_created(null);
        user.setAdmin(false);

        // TODO TEST CODE
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference document = db.collection("data").document();
        documentRef.set(user).addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Successful", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                }
            }
        });


//        collectionReference.add(user);

//        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
//            @Override
//            public void onSuccess(Void aVoid) {
//                Log.d(TAG, "onSuccess: ");
//                Toast.makeText(getApplicationContext(), "Successful upload data to firestor", Toast.LENGTH_LONG).show();
//            }
//        }).addOnFailureListener(this, new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Log.e(TAG, "onFailure: ");
//                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });

    }

    private void showDialog(){
        progressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(progressBar.getVisibility() == View.VISIBLE){
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void goToLogin(View view) {
        startActivity(new Intent(SingUpActivity.this, SingInActivity.class));
        finish();
    }
}

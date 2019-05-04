package com.mobidroid.englishkids;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mobidroid.englishkids.item.KEY;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";
    private Toolbar toolbar;
    private FirebaseAuth.AuthStateListener stateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        setUpToolbar();
        setupFirebaseAuth();
    }

    private void setUpToolbar() {
        toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.account_save: {

            }
            break;

            case R.id.account_exit: {
                AlertSingOut();
            }
            break;

            case android.R.id.home: {
                finish();
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void AlertSingOut() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(AccountActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(AccountActivity.this);
        }
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.do_you_want_to_exit));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FirebaseAuth.getInstance().signOut();
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
                    finish();
                }
                // ...
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(stateListener);
        //adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (stateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(stateListener);
        }
        //adapter.stopListening();
    }
}

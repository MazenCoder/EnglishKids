package com.mobidroid.englishkids.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mobidroid.englishkids.R;
import com.mobidroid.englishkids.item.Check;


public class ResendVerificationDialog extends DialogFragment {

    private static final String TAG = "ResendVerificationDialo";

    //widgets
    private EditText mConfirmPassword, mConfirmEmail;

    //vars
    private Context mContext;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_resend_verification, container, false);
        mConfirmPassword = (EditText) view.findViewById(R.id.confirm_password);
        mConfirmEmail = (EditText) view.findViewById(R.id.confirm_email);
        mContext = getActivity();


        Button confirmDialog = (Button) view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting to resend verification email.");

                if(Check.isNotEmpty(mConfirmEmail)) {
                    if (Check.isValidEmail(mConfirmEmail.getText().toString().toLowerCase().trim())) {

                    if (Check.isNotEmpty(mConfirmPassword)) {

                        if (Check.isValidPassword(mConfirmPassword.getText().toString().trim())) {

                            //temporarily authenticate and resend verification email
                            authenticateAndResendEmail(mConfirmEmail.getText().toString().toLowerCase().trim(),
                                    mConfirmPassword.getText().toString().trim());

                        }else {
                            Check.ToastMessage(getContext(), getString(R.string.password_incorrect));
                        }
                    }else {
//                        Toast.makeText(mContext, "all fields must be filled out", Toast.LENGTH_SHORT).show();
                        Check.ToastMessage(getContext(), getString(R.string.enter_your_pass));
                    }

                    }else {
                        Check.ToastMessage(getContext(), getString(R.string.email_incorrect));
                    }
                }else{
                    Check.ToastMessage(getContext(), getString(R.string.enter_your_email));
                }

            }
        });

        // Cancel button for closing the dialog
        Button cancelDialog = (Button) view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return view;
    }


    /**
     * reauthenticate so we can send a verification email again
     * @param email
     * @param password
     */
    private void authenticateAndResendEmail(String email, String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: reauthenticate success.");
                            sendVerificationEmail();
                            FirebaseAuth.getInstance().signOut();

                            getDialog().dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Check.ToastMessage(getContext(), getString(R.string.invalid_credentials));
                getDialog().dismiss();
            }
        });
    }

    /**
     * sends an email verification link to the user
     */
    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(mContext, "Sent Verification Email", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(mContext, "couldn't send email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

}


















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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.mobidroid.englishkids.R;
import com.mobidroid.englishkids.item.Check;


public class PasswordResetDialog extends DialogFragment {

    private static final String TAG = "PasswordResetDialog";

    //widgets
    private EditText mEmail;

    //vars
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_resetpassword, container, false);
        mEmail = (EditText) view.findViewById(R.id.email_password_reset);
//        mContext = getActivity();

        Button confirmDialog = (Button) view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Check.isNotEmpty(mEmail)){
                    if (Check.isValidEmail(mEmail.getText().toString().toLowerCase().trim())){
                        Log.d(TAG, "onClick: attempting to send reset link to: " + mEmail.getText().toString());
                        sendPasswordResetEmail(mEmail.getText().toString().trim());
                    }else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.email_incorrect), Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.enter_your_email), Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    /**
     * Send a password reset link to the email provided
     * @param email
     */
    public void sendPasswordResetEmail(String email){
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Password Reset Email sent.");
                            Toast.makeText(mContext, getString(R.string.sent_password_reset),
                                    Toast.LENGTH_SHORT).show();
                            getDialog().dismiss();
                        }else{
                            Log.e(TAG, task.getException().getMessage());
                            Toast.makeText(mContext, getString(R.string.no_user_is_associated), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}


















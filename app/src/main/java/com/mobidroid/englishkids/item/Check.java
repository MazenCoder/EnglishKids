package com.mobidroid.englishkids.item;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.text.TextUtils.isEmpty;


/**
 * Created by ModiDroid on 2/10/18.
 */

public class Check {

    public static boolean doStringsMatch(String pass1, String pass2) {
        if (pass1.equals(pass2)) {
            return true;
        }else {
            return false;
        }
    }

    public static boolean isValidPassword(String pass) {
        Pattern PASSWORD_PATTERN
                = Pattern.compile(
                "[a-zA-Z0-9\\!\\@\\#\\$]{8,24}"
        );
        return PASSWORD_PATTERN.matcher(pass).matches();
    }

    public static boolean isValidEmail(String email) {
        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }

    public static boolean isNotEmpty(EditText editText) {

        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
            return false;
        }else {
            return true;
        }
    }

    public static void ToastMessage(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

//    public static void Message(Context context, String msg, View view) {
//
//        // Check if we're running on Android 5.0 or higher
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//            // Call some material design APIs here
//            Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
//        }
//    }

    public static void MessageSnackBar(View viewById,String msg) {
        Snackbar.make(viewById, msg, Snackbar.LENGTH_LONG).show();
    }

    public static void showDialog(ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
    }

    public static void hideDialog(ProgressBar progressBar) {
        if (progressBar.getVisibility() == View.VISIBLE) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void showSoftKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager)
                    view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

//    public void hideSoftKeyboard(){
//        View view = this.getCurrentFocus();
//        if (view != null) {
//            InputMethodManager imm = (InputMethodManager)
//                    view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//        }
//    }

//    private void makeSnackBarMessage(String message){
//        Snackbar.make(mParentLayout, message, Snackbar.LENGTH_SHORT).show();
//    }
}

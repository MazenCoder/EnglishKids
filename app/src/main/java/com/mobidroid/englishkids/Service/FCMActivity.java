package com.mobidroid.englishkids.Service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mobidroid.englishkids.R;
import com.mobidroid.englishkids.fcm.Data;
import com.mobidroid.englishkids.fcm.FirebaseCloudMessage;
import com.mobidroid.englishkids.interface_app.FCM;
import com.mobidroid.englishkids.item.KEY;


import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FCMActivity extends AppCompatActivity {

    private TextInputLayout Til_Fcm_Title, Til_Fcm_Text;
    private TextInputEditText Tie_Fcm_Title, Tie_Fcm_Text;
    private static final String TAG = "FCMActivity";
    private String server_key;

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(LocalHelper.onActivity(newBase));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fcm);

        getServerKey();
        init();
    }

    private void init() {
        Til_Fcm_Title = (TextInputLayout) findViewById(R.id.til_fcm_title);
        Til_Fcm_Text  = (TextInputLayout) findViewById(R.id.til_fcm_text);

        Tie_Fcm_Title = (TextInputEditText) findViewById(R.id.tie_fcm_title);
        Tie_Fcm_Text  = (TextInputEditText) findViewById(R.id.tie_fcm_text);
    }

    public void sendMessage(View view) {
        sendMessageToken(
                Tie_Fcm_Title.getText().toString(),
                Tie_Fcm_Text.getText().toString()
        );
    }

    public void getServerKey() {
        try {
            final FirebaseFirestore db = FirebaseFirestore.getInstance();
            // Source can be CACHE, SERVER, or DEFAULT.
//            Source source = Source.CACHE;
            db.collection(KEY.SERVER)
                    .get()
                    .addOnCompleteListener(this, new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            server_key = document.getString(KEY.SERVER_KEY);
                            Log.e(TAG, "onComplete server key is: "+server_key);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "getDetailsUser: "+e.getMessage());
        }

    }

    private void sendMessageToken(String title, String message) {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            // Do whatever
            try {
                if (!TextUtils.isEmpty(server_key)) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(KEY.BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    FCM fcmAPI = retrofit.create(FCM.class);

                    // attach the header
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key="+server_key);

                    // send the message to all token

                    Data data = new Data();
                    data.setTitle(title);
                    data.setMessage(message);
                    data.setData_type(getString(R.string.data_type_admin_broadcast));

                    FirebaseCloudMessage firebaseCloudMessage = new FirebaseCloudMessage();
                    firebaseCloudMessage.setData(data);
                    firebaseCloudMessage.setTo("/topics/TOPIC_ENGLISH_FUN");
                    Call<ResponseBody> call = fcmAPI.send(headers, firebaseCloudMessage);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Log.d(TAG, "onResponse: "+response.toString());
                            //  Dump the fields
                            Tie_Fcm_Title.setText("");
                            Tie_Fcm_Text.setText("");
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, "onFailure: "+t.getMessage());
                        }
                    });
                }
            }catch (Exception e) {
                Log.e(TAG, "sendMessageToken: error: "+e.getMessage());
            }
        }

    }

}

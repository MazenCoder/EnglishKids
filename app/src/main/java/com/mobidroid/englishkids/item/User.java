package com.mobidroid.englishkids.item;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

@IgnoreExtraProperties
public class User implements Serializable {

    private String uid;
    private String name;
    private String email;
    private String password;
    private String image_path;
    private String image_uri;
    private @ServerTimestamp Date time_created;
    private boolean admin;
    private String messaging_token;

    public User() { }

//    public User(String uid, String name, String email, String password,
//                String phone, String city, Date time_created) {
//
//        this.uid = uid;
//        this.name = name;
//        this.email = email;
//        this.password = password;
//        this.phone = phone;
//        this.city = city;
//        this.time_created = time_created;
//    }

    public String getMessaging_token() {
        return messaging_token;
    }

    public void setMessaging_token(String messaging_token) {
        this.messaging_token = messaging_token;
    }

    public User(String uid, String name, String email, String password,
                String image_path, String image_uri, Date time_created,
                boolean admin, String messaging_token) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.password = password;
        this.image_path = image_path;
        this.image_uri = image_uri;
        this.time_created = time_created;
        this.admin = admin;
        this.messaging_token = messaging_token;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public Date getTime_created() {
        return time_created;
    }

    public void setTime_created(Date time_created) {
        this.time_created = time_created;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

//    public String getPhone() {
//        return phone;
//    }
//
//    public void setPhone(String phone) {
//        this.phone = phone;
//    }
//
//    public String getCity() {
//        return city;
//    }
//
//    public void setCity(String city) {
//        this.city = city;
//    }

    public String getImage_path() {
        return image_path;
    }

    public void setImage_path(String image_path) {
        this.image_path = image_path;
    }

    public String getImage_uri() {
        return image_uri;
    }

    public void setImage_uri(String image_uri) {
        this.image_uri = image_uri;
    }


//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(this.uid);
//        dest.writeString(this.name);
//        dest.writeString(this.email);
//        dest.writeString(this.password);
//        dest.writeString(this.phone);
//        dest.writeString(this.city);
//        dest.writeString(this.image_path);
//        dest.writeString(this.image_uri);
//        dest.writeSerializable(this.time_created);
////        dest.writeLong(this.time_created != null ? this.time_created.getTime() : -1);
//        dest.writeByte(this.admin ? (byte) 1 : (byte) 0);
//    }
//
//    protected User(Parcel in) {
//        this.uid = in.readString();
//        this.name = in.readString();
//        this.email = in.readString();
//        this.password = in.readString();
//        this.phone = in.readString();
//        this.city = in.readString();
//        this.image_path = in.readString();
//        this.image_uri = in.readString();
////        long tmpTime_created = in.readLong();
//        this.time_created = (Date) in.readSerializable();
////        this.time_created = tmpTime_created == -1 ? null : new Date(tmpTime_created);
//        this.admin = in.readByte() != 0;
//    }
//
//    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
//        @Override
//        public User createFromParcel(Parcel source) {
//            return new User(source);
//        }
//
//        @Override
//        public User[] newArray(int size) {
//            return new User[size];
//        }
//    };
}

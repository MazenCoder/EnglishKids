package com.mobidroid.englishkids.fcm;

public class Data {
    private String title;
    private String message;
    private String data_type;

    public Data() {
    }

    public Data(String title, String message, String data_type) {
        this.title = title;
        this.message = message;
        this.data_type = data_type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData_type() {
        return data_type;
    }

    public void setData_type(String data_type) {
        this.data_type = data_type;
    }
}

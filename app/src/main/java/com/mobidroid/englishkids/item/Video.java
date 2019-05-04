package com.mobidroid.englishkids.item;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class Video implements Serializable {

    private String id;
    private String title_course;
    private String title_video;
    private String uri_video;
    private String link_video;
    private String description;
    private @ServerTimestamp Date time_created;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle_course() {
        return title_course;
    }

    public void setTitle_course(String title_course) {
        this.title_course = title_course;
    }

    public String getUri_video() {
        return uri_video;
    }

    public void setUri_video(String uri_video) {
        this.uri_video = uri_video;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTime_created() {
        return time_created;
    }

    public void setTime_created(Date time_created) {
        this.time_created = time_created;
    }

    public String getTitle_video() {
        return title_video;
    }

    public void setTitle_video(String title_video) {
        this.title_video = title_video;
    }

    public String getLink_video() {
        return link_video;
    }

    public void setLink_video(String link_video) {
        this.link_video = link_video;
    }
}

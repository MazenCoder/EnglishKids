package com.mobidroid.englishkids.item;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Images {

    private Map<String, String> images_uri_map = new HashMap<>();
    private @ServerTimestamp Date time_created;
//    private List<String> uriList = new ArrayList<>();

    public Images() { }


//    public List<String> getUriList() {
//        return uriList;
//    }
//
//    public void setUriList(List<String> uriList) {
//        this.uriList = uriList;
//    }

    public Map<String, String> getImages_uri_map() {
        return images_uri_map;
    }

    public void setImages_uri_map(Map<String, String> images_uri_map) {
        this.images_uri_map = images_uri_map;
    }

    public Date getTime_created() {
        return time_created;
    }

    public void setTime_created(Date time_created) {
        this.time_created = time_created;
    }
}

package com.aakash.aadhan.model;

import android.util.Log;
import com.google.gson.annotations.SerializedName;

public class NotificationRequest {
    private static final String TAG = "NotificationRequest";

    @SerializedName("title")
    private String title;

    @SerializedName("body")
    private String body;

    @SerializedName("image")
    private String image;

    public NotificationRequest(String title, String body, String image) {
        this.title = title;
        this.body = body;
        this.image = image != null ? image : "";
        Log.d(TAG, "NotificationRequest created: " + toString());
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        Log.d(TAG, "Title set: " + title);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
        Log.d(TAG, "Body set: " + body);
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image != null ? image : "";
        Log.d(TAG, "Image set: " + this.image);
    }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "title='" + title + '\'' +
                ", body='" + body + '\'' +
                ", image='" + (image.length() > 50 ? image.substring(0, 50) + "..." : image) + '\'' +
                '}';
    }
}

package com.bytedance.androidcamp.network.dou.model;

import com.google.gson.annotations.SerializedName;

public class Feeds {
    @SerializedName("student_id")
    String student_id;
    @SerializedName("user_name")
    String user_name;
    @SerializedName("image_url")
    String image_url;
    @SerializedName("_id")
    String _id;
    @SerializedName("video_url")
    String video_url;
    @SerializedName("createdAt")
    String createdAt;
    @SerializedName("updatedAt")
    String updatedAt;

    public String getVideo_url() {
        return video_url;
    }

    public String get_id() {
        return _id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getStudent_id() {
        return student_id;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getUser_name() {
        return user_name;
    }
}
package com.bytedance.androidcamp.network.dou.model;

import com.google.gson.annotations.SerializedName;

public class PostVideoResponse {
    @SerializedName("result") private Result result;
    @SerializedName("url") private String url;
    @SerializedName("success") private boolean success;

    public Result getResult() {
        return result;
    }

    public String getUrl() {
        return url;
    }

    public boolean getSuccess() {
        return success;
    }

    public String getInfo() {
        return getResult()+"\n"+getUrl()+"\n"+getSuccess();
    }
}

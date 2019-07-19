package com.bytedance.androidcamp.network.dou.model;

import android.widget.ListView;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GetVideosResponse {
    @SerializedName("feeds") Feeds feeds[];
    @SerializedName("success") boolean success;

    private List<Video> videos = new ArrayList<>();
    private List<Video> my_videos = new ArrayList<>();

    public List<Video> getVideos() {
        my_videos.clear();
        if (success == true) {
            for (int i=0;i!=feeds.length;i++) {
                Video video = new Video();

                video.setStudentId(feeds[i].getStudent_id());
                video.setUserName(feeds[i].getUser_name());
                video.setImageUrl(feeds[i].getImage_url());
                video.setVideoUrl(feeds[i].getVideo_url());

                videos.add(video);
            }

        }

        return videos;
    }


    public List<Video> getOnesVideos(String userName) {
        my_videos.clear();
        if (success == true) {
            for (int i=0;i!=videos.size();i++) {
                Video video = videos.get(i);

                if(video.getUserName().equals(userName)){
                    my_videos.add(video);
                }
            }
        }

        return my_videos;
    }

    public Feeds[] getFeeds() {
        return feeds;
    }

    public boolean getSuccess() {
        return success;
    }
}

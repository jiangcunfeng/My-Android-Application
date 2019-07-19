package com.bytedance.androidcamp.network.dou;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.GetVideosResponse;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.dou.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private RecyclerView mRv;
    private List<Video> mVideos = new ArrayList<>();
    private ImageView img_refresh;
    private EditText editText;
    private ImageView img_search;


    private String username = "hhh";

    private Retrofit retrofit;
    private IMiniDouyinService miniDouyinService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_search);

        initRetrofit();
        initRecyclerView();
        initListener();
        initBtns();

        fetchFeed();

    }



    // TODO 8: initialize retrofit & miniDouyinService
    private void initRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(IMiniDouyinService.HOST)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (miniDouyinService == null) {
            miniDouyinService = retrofit.create(IMiniDouyinService.class);
        }
    }

    private void initBtns() {
        img_refresh = findViewById(R.id.img_refresh_search);
        img_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchFeed();
            }
        });


        editText = findViewById(R.id.edit_text_search);

        img_search = findViewById(R.id.img_search_search);
        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = editText.getText().toString();
                fetchFeed();
            }
        });
    }


//========================================================================/**

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView img;
        public ImageView img_header;
        public TextView text_author;
        public TextView text_video_msg;
        public VideoView video_view;
        public TextView text_heart;
        public ImageView imgPlay;
        public ImageView img_love;
        public boolean isLove;
        public ImageView img_heart;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.img_thumb);
            img_header = itemView.findViewById(R.id.img_header);
            text_author = itemView.findViewById(R.id.text_author_msg);
            text_video_msg = itemView.findViewById(R.id.text_video_msg);
            video_view = itemView.findViewById(R.id.video_view);
            text_heart = itemView.findViewById(R.id.text_heart);
            imgPlay = itemView.findViewById(R.id.img_play);
            img_love = itemView.findViewById(R.id.img_love);
            isLove = false;
            img_heart = itemView.findViewById(R.id.img_heart);
        }

        public void bind(final Activity activity, final Video video) {
//            ImageHelper.displayWebImage(video.getImageUrl(), img);
            //todo 选择获得的图片的缩略图作为封面和头像，显示作者和时间
            Glide.with(img.getContext()).load(video.getImageUrl()).thumbnail(0.1f).into(img);
            Glide.with(img_header.getContext()).load(video.getImageUrl()).thumbnail(0.05f).into(img_header);

            text_author.setText("@"+video.getUserName());
            text_video_msg.setText(video.getStudentId());
            video_view.setVideoPath(video.getVideoUrl());


            img_heart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!isLove){
                        Toast.makeText(SearchActivity.this,"添加到喜欢", Toast.LENGTH_SHORT).show();
                        Animation loadAnimation = AnimationUtils.loadAnimation(SearchActivity.this,R.anim.heart_holder);
                        img_love.startAnimation(loadAnimation);
                        loadAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                img_love.setAlpha(1f);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                img_love.setAlpha(0f);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });

                        img_heart.setColorFilter(Color.RED);
                        isLove = true;
                    }else {
                        Toast.makeText(SearchActivity.this,"已取消喜欢", Toast.LENGTH_SHORT).show();

                        img_heart.setColorFilter(Color.WHITE);
                        isLove = false;
                    }

                }

            });

        }
    }

    private void initRecyclerView() {
        mRv = findViewById(R.id.recycler_search);
//        mRv.setLayoutManager(new ViewPagerLayoutManager(this, OrientationHelper.VERTICAL));
        mLayoutManager = new ViewPagerLayoutManager(this, OrientationHelper.VERTICAL);
        mRv.setLayoutManager(mLayoutManager);
        mRv.setAdapter(new RecyclerView.Adapter<SearchActivity.MyViewHolder>() {
            @NonNull
            @Override
            public SearchActivity.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new SearchActivity.MyViewHolder(
                        LayoutInflater.from(SearchActivity.this)
                                .inflate(R.layout.item_view_pager, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull SearchActivity.MyViewHolder viewHolder, int i) {
                final Video video = mVideos.get(i);
                viewHolder.bind(SearchActivity.this, video);
            }

            @Override
            public int getItemCount() {
                return mVideos.size();
            }
        });
////todo PagerSnapHelper()
//        new PagerSnapHelper().attachToRecyclerView(mRv);
    }

    private ViewPagerLayoutManager mLayoutManager;
    private void initListener(){
        mLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {

            @Override
            public void onInitComplete() {
                Log.e(TAG,"onInitComplete");
                playVideo(0);
            }

            @Override
            public void onPageRelease(boolean isNext,int position) {
                Log.e(TAG,"释放位置:"+position +" 下一页:"+isNext);
                int index = 0;
                if (isNext){
                    index = 0;
                }else {
                    index = 1;
                }
                releaseVideo(index);
            }

            @Override
            public void onPageSelected(int position,boolean isBottom) {
                Log.e(TAG,"选中位置:"+position+"  是否是滑动到底部:"+isBottom);
                playVideo(0);
            }


        });
    }

    private void playVideo(int position) {
        View itemView = mRv.getChildAt(0);
        final VideoView videoView = itemView.findViewById(R.id.video_view);
        final ImageView imgPlay = itemView.findViewById(R.id.img_play);
        final ImageView imgThumb = itemView.findViewById(R.id.img_thumb);
        final RelativeLayout rootView = itemView.findViewById(R.id.root_view);
        final MediaPlayer[] mediaPlayer = new MediaPlayer[1];
        videoView.start();
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                mediaPlayer[0] = mp;
                mp.setLooping(true);
                imgThumb.animate().alpha(0).setDuration(100).start();
                return false;
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

            }
        });


        imgPlay.setOnClickListener(new View.OnClickListener() {
            boolean isPlaying = true;
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()){
                    imgPlay.animate().alpha(0.3f).start();
                    videoView.pause();
                    isPlaying = false;
                }else {
                    imgPlay.animate().alpha(0f).start();
                    videoView.start();
                    isPlaying = true;
                }
            }
        });
    }

    private void releaseVideo(int index){
        View itemView = mRv.getChildAt(index);
        final VideoView videoView = itemView.findViewById(R.id.video_view);
        final ImageView imgThumb = itemView.findViewById(R.id.img_thumb);
        final ImageView imgPlay = itemView.findViewById(R.id.img_play);
        videoView.stopPlayback();
        imgThumb.animate().alpha(1).start();
        imgPlay.animate().alpha(0f).start();
    }
//==========================================
//========================================================================


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult() called with: requestCode = ["
                + requestCode
                + "], resultCode = ["
                + resultCode
                + "], data = ["
                + data
                + "]");
    }

    public void fetchFeed() {
        // TODO 10: get videos & update recycler list
        Call<GetVideosResponse> call = miniDouyinService.getVideos();
        call.enqueue(new Callback<GetVideosResponse>() {
            @Override
            public void onResponse(Call<GetVideosResponse> call, Response<GetVideosResponse> response) {
                if (response.body() != null && response.body().getVideos() != null) {
//                    mVideos = response.body().getVideos();
                    Toast.makeText(SearchActivity.this,"refresh",Toast.LENGTH_SHORT);

                    mVideos = response.body().getOnesVideos(username);
                    if(mVideos==null){
                        Toast.makeText(SearchActivity.this,"find no video",Toast.LENGTH_LONG);
                    }

                    mRv.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<GetVideosResponse> call, Throwable throwable) {
                String s = "TODO 10: get videos & update recycler list" + throwable.getMessage();
                Toast.makeText(SearchActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });

    }
}


package com.bytedance.androidcamp.network.dou;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.Feeds;
import com.bytedance.androidcamp.network.dou.model.GetVideosResponse;
import com.bytedance.androidcamp.network.dou.model.PostVideoResponse;
import com.bytedance.androidcamp.network.dou.model.Video;
import com.bytedance.androidcamp.network.dou.util.Utils;
import com.bytedance.androidcamp.network.lib.util.ImageHelper;
import com.bytedance.androidcamp.network.dou.util.ResourceUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

//import okhttp3.Callback;
import retrofit2.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView mRv;
    private List<Video> mVideos = new ArrayList<>();
    private ImageView img_add;
    private ImageView img_refresh;
    private TextView text_mine;

    private ImageView img_search;

    // 权限
    private String[] mPermissionsArrays = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};
    private final static int REQUEST_PERMISSION = 123;

    private Retrofit retrofit;
    private IMiniDouyinService miniDouyinService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_view_pager_layout_manager);

        initRetrofit();
        initRecyclerView();
        initListener();
        initBtns();

        fetchFeed();
    }



    // TODO 8: initialize retrofit & miniDouyinService
    private void initRetrofit() {
//        textView = findViewById(R.id.textView);
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
        img_refresh = findViewById(R.id.img_refresh);
        img_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchFeed();
            }
        });

        img_add = findViewById(R.id.img_add);
        img_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!Utils.isPermissionsReady(MainActivity.this,mPermissionsArrays)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Utils.reuqestPermissions(MainActivity.this,mPermissionsArrays, REQUEST_PERMISSION);
                    }
                }else{
                    startActivity(new Intent(MainActivity.this, CustomCameraActivity.class));
                }

            }
        });

        img_search = findViewById(R.id.img_search);
        img_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });


        text_mine = findViewById(R.id.text_mine);
        text_mine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MineActivity.class);
                startActivity(intent);
            }
        });

    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(Utils.isPermissionsReady(MainActivity.this,mPermissionsArrays)){
            startActivity(new Intent(MainActivity.this, CustomCameraActivity.class));
        }
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
                            Toast.makeText(MainActivity.this,"添加到喜欢", Toast.LENGTH_SHORT).show();
                            Animation loadAnimation = AnimationUtils.loadAnimation(MainActivity.this,R.anim.heart_holder);
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
                            Toast.makeText(MainActivity.this,"已取消喜欢", Toast.LENGTH_SHORT).show();

                            img_heart.setColorFilter(Color.WHITE);
                            isLove = false;
                        }

                    }

                });

        }
    }

    private void initRecyclerView() {
        mRv = findViewById(R.id.recycler);
//        mRv.setLayoutManager(new ViewPagerLayoutManager(this, OrientationHelper.VERTICAL));
        mLayoutManager = new ViewPagerLayoutManager(this, OrientationHelper.VERTICAL);
        mRv.setLayoutManager(mLayoutManager);
        mRv.setAdapter(new RecyclerView.Adapter<MyViewHolder>() {
            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new MyViewHolder(
                        LayoutInflater.from(MainActivity.this)
                                .inflate(R.layout.item_view_pager, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
                final Video video = mVideos.get(i);
                viewHolder.bind(MainActivity.this, video);
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
                imgThumb.animate().alpha(0).setDuration(50).start();
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
                    mVideos = response.body().getVideos();
                    Toast.makeText(MainActivity.this,"refresh",Toast.LENGTH_SHORT);

//                    mVideos = response.body().getOnesVideos("hhh");

                    mRv.getAdapter().notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<GetVideosResponse> call, Throwable throwable) {
                String s = "TODO 10: get videos & update recycler list" + throwable.getMessage();
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });

    }
}

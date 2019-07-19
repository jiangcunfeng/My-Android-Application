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

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final String TAG = "MainActivity";
    private RecyclerView mRv;
    private List<Video> mVideos = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public Button mBtn;
    private Button mBtnRefresh;
    private ImageView img_add;
    private ImageView img_refresh;

    private ImageView img_search;

    //手势
    private GestureDetector gestureDetector;

    private String[] mPermissionsArrays = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};
    private final static int REQUEST_PERMISSION = 123;

//    private TextView textView;

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


        //设置手势
        setGestureDetector();

        fetchFeed();
    }






    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    //手势方法
    public void setGestureDetector() {
        gestureDetector = new GestureDetector(this,
                new GestureDetector.OnGestureListener() {
                    //默认重载
                    @Override
                    public boolean onDown(MotionEvent motionEvent) {
                        return false;
                    }

                    @Override
                    public void onShowPress(MotionEvent motionEvent) {

                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent motionEvent) {
                        return false;
                    }

                    @Override
                    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                        Toast.makeText(MainActivity.this,"fling", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent motionEvent) {
                        Toast.makeText(MainActivity.this,"longpress", Toast.LENGTH_SHORT).show();
                    }

                    // 自定义滚动事件
                    // MotionEvent e1 手势起点事件
                    // MotionEvent e2 手势终点
                    // float distanceX X轴方向上移动的速度/每秒
                    // float distanceY Y轴方向上移动的速度/每秒
                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
                        //上划
                        if (e1.getY() - e2.getY() > 0.5 && Math.abs(distanceY) > 0.5) {
                            if (e1.getX() > 500) {
                                addLightness(20);
                            } else {
                                addVolume(1);
                            }
                        }
                        //下划
                        if (e1.getY() - e2.getY() < 0.5 && Math.abs(distanceY) > 0.5) {
                            if (e1.getX() > 500) {
                                addLightness(-20);
                            } else {
                                addVolume(-1);
                            }
                        }
                        return true;
                    }

                }
        );
    }

    // 改变屏幕亮度
    public void addLightness(float lightness) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        //修改屏幕的亮度（最大是255）
        layoutParams.screenBrightness += lightness / 255f;
        if (layoutParams.screenBrightness > 1) {
            layoutParams.screenBrightness = 1;
        } else if (layoutParams.screenBrightness < 0.2) {
            layoutParams.screenBrightness = 0.2f;
        }
        getWindow().setAttributes(layoutParams);
    }

    // 改变音量
    public void addVolume(int volume) {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //当前音量
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //最大音量
        int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 修改音量
        currentVolume += volume;
        if (currentVolume < 0) {
            currentVolume = 0;
        }
        if (currentVolume > max) {
            currentVolume = max;
        }

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, AudioManager.FLAG_PLAY_SOUND);
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
//        mBtn = findViewById(R.id.btn);
//        mBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String s = mBtn.getText().toString();
//                if (getString(R.string.select_an_image).equals(s)) {
//                    chooseImage();
//                } else if (getString(R.string.select_a_video).equals(s)) {
//                    chooseVideo();
//                } else if (getString(R.string.post_it).equals(s)) {
//                    if (mSelectedVideo != null && mSelectedImage != null) {
//                        postVideo();
//                    } else {
//                        throw new IllegalArgumentException("error data uri, mSelectedVideo = "
//                                + mSelectedVideo
//                                + ", mSelectedImage = "
//                                + mSelectedImage);
//                    }
//                } else if ((getString(R.string.success_try_refresh).equals(s))) {
//                    mBtn.setText(R.string.select_an_image);
//                }
//            }
//        });
//
//        mBtnRefresh = findViewById(R.id.btn_refresh);
//        mBtnRefresh.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                fetchFeed();
//            }
//        });


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

//        img_search = findViewById(R.id.img_search);
//        img_search.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, HotMsgActivity.class);
//                startActivity(intent);
//            }
//        });

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


        //todo 选择视频缩略图作为预览
        public void bind(final Activity activity, final Video video) {
//            ImageHelper.displayWebImage(video.getImageUrl(), img);
            //todo 选择获得的图片的缩略图作为封面和头像，显示作者和时间
            Glide.with(img.getContext()).load(video.getImageUrl()).thumbnail(0.1f).into(img);
            Glide.with(img_header.getContext()).load(video.getImageUrl()).thumbnail(0.1f).into(img_header);

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



            //todo

//            img.animate().alpha(0).setDuration(200).start();
//            video_view.start();
//            video_view.requestFocus();
//
//            //循环播放
//            video_view.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mPlayer) {
//                    mPlayer.start();
//                    mPlayer.setLooping(true);
//                }
//            });

            {
//            String videoPath = video.getVideoUrl();
//            MediaMetadataRetriever media = new MediaMetadataRetriever();
//
//            media.setDataSource(videoPath);
//
//
//            Bitmap bitmap = media.getFrameAtTime(1);
//            img.setImageBitmap(bitmap);

//            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, MINI_KIND);

//            if(bitmap!=null){
//                Log.d("jcf", "not null ");
//
//            }else {
//                Log.d("jcf", "null ");
//
//            }
//            Log.d("jcf", "video_path "+videoPath);
//            Bitmap bitmap = getVideoThumbnail(videoPath,MINI_KIND,50,50);
                //glide加载bitmap
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//            byte[] bytes=baos.toByteArray();
//            Glide.with(img_header.getContext()).load(bytes).centerCrop().into(img_header);

//            Drawable drawable=new BitmapDrawable(bitmap);
//            Glide.with(img.getContext()).load(drawable).into(img);

//            String path = Environment.getExternalStorageDirectory().getPath()+"/Test";
//            String img_thumb_path = SavaImage(bitmap, path);
//            Glide.with(img.getContext()).load(img_thumb_path).into(img);
            }
//            img.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    VideoActivity.launch(activity, video.getVideoUrl());
//                }
//            });
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
                    imgPlay.animate().alpha(0.15f).start();
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
//==========================================
//==========================================
//==========================================
    public static String SavaImage(Bitmap bitmap, String path){
        File file=new File(path);
        FileOutputStream fileOutputStream=null;


        String newImgPath = path+"/"+System.currentTimeMillis()+".jpeg";
        //文件夹不存在，则创建它
        if(!file.exists()){
            file.mkdir();
        }
        try {
            fileOutputStream=new FileOutputStream(newImgPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100,fileOutputStream);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("jcf", "imgthumbPath"+newImgPath);

        return newImgPath;
    }

    /*
     * 获取视频的缩略图
     * 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     * @param videoPath 视频的路径
     * @param kind 参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
//如果指定的视频的宽高都大于了MICRO_KIND的大小，那么就使用MINI_KIND就可以了
    public static Bitmap getVideoThumbnail(String videoPath, int kind, int width, int height) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind); //調用ThumbnailUtils類的靜態方法createVideoThumbnail獲取視頻的截圖；
        if(bitmap!= null){
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
                    ThumbnailUtils.OPTIONS_RECYCLE_INPUT);//調用ThumbnailUtils類的靜態方法extractThumbnail將原圖片（即上方截取的圖片）轉化為指定大小；
        }
        return bitmap;
    }
//========================================================================






    public void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE);
    }

    public void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO);
    }

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

//        if (resultCode == RESULT_OK && null != data) {
//            if (requestCode == PICK_IMAGE) {
//                mSelectedImage = data.getData();
//                Log.d(TAG, "selectedImage = " + mSelectedImage);
//                mBtn.setText(R.string.select_a_video);
//            } else if (requestCode == PICK_VIDEO) {
//                mSelectedVideo = data.getData();
//                Log.d(TAG, "mSelectedVideo = " + mSelectedVideo);
//                mBtn.setText(R.string.post_it);
//            }
//        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        try {
            File f = new File(ResourceUtils.getRealPath(MainActivity.this, uri));
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
            return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
        } catch (Exception e) {
//            textView.setText("TAT getMultipartFromUri: " + uri.toString() + "\ne.getMessage(): " + e.getMessage());
        }
        return null;
    }

    private void postVideo() {
//        mBtn.setText("POSTING...");
//        mBtn.setEnabled(false);
//        mSelectedImage = ResourceUtils.getFileUri(this, mSelectedImage);
//        mSelectedVideo = ResourceUtils.getFileUri(this, mSelectedVideo);
//        textView.setText(mSelectedImage.toString());
        MultipartBody.Part coverImagePart = getMultipartFromUri("cover_image", mSelectedImage);
        MultipartBody.Part videoPart = getMultipartFromUri("video", mSelectedVideo);
//        textView.setText(mSelectedImage.toString() + "\n" + mSelectedVideo.toString());
        // TODO 9: post video & update buttons
        Call<PostVideoResponse> call = miniDouyinService.postVideo("233", "hhh", coverImagePart, videoPart);
        call.enqueue(new Callback<PostVideoResponse>() {
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                if (response.body() != null && response.body().getSuccess() == true) {
//                    mBtn.setText(R.string.success_try_refresh);
//                    mBtn.setEnabled(true);
//                    textView.setText("^_^ TODO 9");
                } else {
//                    mBtn.setText(R.string.select_an_image);
//                    mBtn.setEnabled(true);
//                    textView.setText("TAT TODO 9");
                    if(response.body() != null){
//                        textView.setText("TAT TODO 9: "+"\n"+response.body().getInfo());
                    }
                }
            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable throwable) {
//                mBtn.setText(R.string.select_an_image);
//                mBtn.setEnabled(true);

                String msg = "TODO 9: post video & update buttons" + throwable.getMessage();
//                textView.setText(msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void fetchFeed() {
//        mBtnRefresh.setText("requesting...");
//        mBtnRefresh.setEnabled(false);
        // TODO 10: get videos & update recycler list
        Call<GetVideosResponse> call = miniDouyinService.getVideos();
        call.enqueue(new Callback<GetVideosResponse>() {
            @Override
            public void onResponse(Call<GetVideosResponse> call, Response<GetVideosResponse> response) {
                if (response.body() != null && response.body().getVideos() != null) {
                    mVideos = response.body().getVideos();
                    mRv.getAdapter().notifyDataSetChanged();

//                    mBtnRefresh.setText(R.string.refresh_feed);
//                    mBtnRefresh.setEnabled(true);
//                    textView.setText("^_^ TODO 10");
                } else {
//                    mBtnRefresh.setText("refresh fail");
//                    mBtnRefresh.setEnabled(true);
//                    textView.setText("TAT TODO 10");
                }
            }

            @Override
            public void onFailure(Call<GetVideosResponse> call, Throwable throwable) {
//                mBtnRefresh.setText(R.string.refresh_feed);
//                mBtnRefresh.setEnabled(true);

                String s = "TODO 10: get videos & update recycler list" + throwable.getMessage();
//                textView.setText(s);
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });

    }
}

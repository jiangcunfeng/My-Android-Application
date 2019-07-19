package com.bytedance.androidcamp.network.dou;

import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.bytedance.androidcamp.network.dou.api.IMiniDouyinService;
import com.bytedance.androidcamp.network.dou.model.PostVideoResponse;
import com.bytedance.androidcamp.network.dou.util.ResourceUtils;
import com.bytedance.androidcamp.network.dou.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.bytedance.androidcamp.network.dou.util.Utils.MEDIA_TYPE_IMAGE;
import static com.bytedance.androidcamp.network.dou.util.Utils.MEDIA_TYPE_VIDEO;
import static com.bytedance.androidcamp.network.dou.util.Utils.getOutputMediaFile;

public class CustomCameraActivity extends AppCompatActivity {

    public Uri mSelectedImage;
    private Uri mSelectedVideo;

    private SurfaceView mSurfaceView;
    private Camera mCamera;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private int rotationDegree = 0;

    private int state = 0;
    private boolean isRecording = false;

    private Camera.AutoFocusCallback autoFocusCallback;

    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;

    private ImageView selectView;
    private ImageView confirmView;
    private ImageView videoView;

    private Retrofit retrofit;
    private IMiniDouyinService miniDouyinService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);

        autoFocusCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
            }
        };

        initRetrofit();
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCamera = getCamera(CAMERA_TYPE);
        //animationView = (LottieAnimationView) findViewById(R.id.animation_view);
        mSurfaceView = findViewById(R.id.img);
        //todo 给SurfaceHolder添加Callback
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                startPreview(surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                releaseCameraAndPreview();
            }
        });
    }

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);
        rotationDegree = getCameraDisplayOrientation(position);
        cam.setDisplayOrientation(rotationDegree);
        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等
        return cam;
    }

    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        //todo 释放camera资源
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    private void startPreview(SurfaceHolder holder) {
        //todo 开始预览
        try{
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }catch(Exception e){
            Log.d("Preview","start exception");
        }

    }

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

    private void initViews() {
        selectView = findViewById(R.id.select);
        selectView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state==0) {
                    chooseVideo();
                    Log.e("select state",state+"");
                } else if (state==1||state==2) {
                    chooseImage();
                    Log.e("select state",state+"");
                } else if (state==3) {
                    Log.e("select state",state+"");
                }
            }
        });

        confirmView = findViewById(R.id.confirm);
        confirmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state==0){
                    state = 1;
                    selectView.setImageResource(R.drawable.image);
                    confirmView.setImageResource(R.drawable.ok);
                    videoView.setImageResource(R.drawable.circle_15);
                    Log.e("confirm state",state+"");
                }else if(state==1||state==2){
                    if(state==1){
                        MediaMetadataRetriever media = new MediaMetadataRetriever();
                        String videoPath = Utils.convertUriToPath(CustomCameraActivity.this,mSelectedVideo);
                        media.setDataSource(videoPath);
                        Bitmap bitmap = media.getFrameAtTime(1,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                        mSelectedImage = Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(),bitmap,null,null));
                    }
                    confirmView.setEnabled(true);
                    if (mSelectedVideo != null && mSelectedImage != null) {
                        postVideo();
                    } else {
                        throw new IllegalArgumentException("error data uri, mSelectedVideo = "
                                + mSelectedVideo
                                + ", mSelectedImage = "
                                + mSelectedImage);
                    }
                    Log.e("confirm state",state+"");
                }else if (state==3) {
                    selectView.setEnabled(true);
                    videoView.setEnabled(true);
                    selectView.setImageResource(R.drawable.video_folder);
                    confirmView.setImageResource(R.drawable.arrow_forward);
                    videoView.setImageResource(R.drawable.circle);
                    state = 0;
                    Log.e("confirm state",state+"");
                    finish();
                }
            }
        });

        videoView = findViewById(R.id.video);
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state==0){
                    if (isRecording) {
                        //todo 停止录制
                        videoView.setImageResource(R.drawable.circle);
                        releaseMediaRecorder();
                        selectView.setEnabled(true);
                        confirmView.setEnabled(true);
                        isRecording = false;
                    } else {
                        //todo 录制
                        selectView.setEnabled(false);
                        confirmView.setEnabled(false);
                        videoView.setImageResource(R.drawable.circle_stop);
                        isRecording = prepareVideoRecorder();
                    }
                    Log.e("confirm state",state+"");
                }else if(state==1||state==2){
                    mCamera.takePicture(null,null,mPicture);
                    state = 2;
                    Log.e("confirm state",state+"");
                } else if (state==3) {
                    Log.e("confirm state",state+"");
                }
            }
        });

        findViewById(R.id.rl_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Camera.Parameters params = mCamera.getParameters();
                if(params.getMaxNumMeteringAreas()>0){
                    mCamera.autoFocus(autoFocusCallback);
                }
                return false;
            }
        });

        findViewById(R.id.img_facing).setOnClickListener(v -> {
            //todo 切换前后摄像头
            if(CAMERA_TYPE==Camera.CameraInfo.CAMERA_FACING_BACK)
                mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            else
                mCamera = getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            startPreview(mSurfaceView.getHolder());
        });
    }

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

        if (resultCode == RESULT_OK && null != data) {
            if (requestCode == PICK_IMAGE) {
                mSelectedImage = data.getData();
                state = 2;
                Log.e("select state",state+"");
            } else if (requestCode == PICK_VIDEO) {
                mSelectedVideo = data.getData();
                state = 0;
                Log.e("select state",state+"");
            }
        }
    }

    private MultipartBody.Part getMultipartFromUri(String name, Uri uri) {
        try {
            File f = new File(ResourceUtils.getRealPath(CustomCameraActivity.this, uri));
            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), f);
            return MultipartBody.Part.createFormData(name, f.getName(), requestFile);
        } catch (Exception e) {
//            textView.setText("TAT getMultipartFromUri: " + uri.toString() + "\ne.getMessage(): " + e.getMessage());
        }
        return null;
    }

    private void postVideo() {
        state = -1;
        selectView.setEnabled(false);
        confirmView.setEnabled(false);
        videoView.setEnabled(false);
        confirmView.setImageResource(R.drawable.wait);
        Log.e("post state",state+"");
//        mSelectedImage = ResourceUtils.getFileUri(this, mSelectedImage);
//        mSelectedVideo = ResourceUtils.getFileUri(this, mSelectedVideo);
//        textView.setText(mSelectedImage.toString());
        MultipartBody.Part coverImagePart = getMultipartFromUri("cover_image", mSelectedImage);
        MultipartBody.Part videoPart = getMultipartFromUri("video", mSelectedVideo);
//        textView.setText(mSelectedImage.toString() + "\n" + mSelectedVideo.toString());
        // TODO 9: post video & update buttons
        if(coverImagePart!=null&&videoPart!=null)Log.e("post","not null");
        Call<PostVideoResponse> call = miniDouyinService.postVideo("233", "hhh", coverImagePart, videoPart);
        call.enqueue(new Callback<PostVideoResponse>() {
            @Override
            public void onResponse(Call<PostVideoResponse> call, Response<PostVideoResponse> response) {
                if (response.body() != null && response.body().getSuccess()) {
                    state = 3;
                    confirmView.setEnabled(true);
                    confirmView.setImageResource(R.drawable.success);
//                    textView.setText("^_^ TODO 9");
                    Log.e("post state",state+"");
                } else if(response.body()==null){
                    state = 0;
                    selectView.setImageResource(R.drawable.video_folder);
                    confirmView.setImageResource(R.drawable.arrow_forward);
                    videoView.setImageResource(R.drawable.circle);
                    selectView.setEnabled(true);
                    confirmView.setEnabled(true);
                    videoView.setEnabled(true);
//                    textView.setText("TAT TODO 9: "+"\n"+response.body().getInfo());
                    Log.e("post state",state+"");
                }else{
                    state = 0;
                    selectView.setImageResource(R.drawable.video_folder);
                    confirmView.setImageResource(R.drawable.arrow_forward);
                    videoView.setImageResource(R.drawable.circle);
                    selectView.setEnabled(true);
                    confirmView.setEnabled(true);
                    videoView.setEnabled(true);
//                    textView.setText("TAT TODO 9, response.body.getSuccess == null");
                    Log.e("post state",state+"");
                }
            }

            @Override
            public void onFailure(Call<PostVideoResponse> call, Throwable throwable) {
                state = 0;
                selectView.setImageResource(R.drawable.video_folder);
                confirmView.setImageResource(R.drawable.arrow_forward);
                videoView.setImageResource(R.drawable.circle);
                selectView.setEnabled(true);
                confirmView.setEnabled(true);
                videoView.setEnabled(true);

                String msg = "TODO 9: post video & update buttons" + throwable.getMessage();
//                textView.setText(msg);
                Toast.makeText(CustomCameraActivity.this, msg, Toast.LENGTH_SHORT).show();
                Log.e("post state",state+"");
            }
        });
    }

    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            mSelectedImage = Uri.fromFile(pictureFile);
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }

        mCamera.startPreview();
    };

    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        //todo 准备MediaRecorder
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.reset();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        File videoFile = getOutputMediaFile(MEDIA_TYPE_VIDEO);
        mMediaRecorder.setOutputFile(videoFile.toString());
        mSelectedVideo = Uri.fromFile(videoFile);

        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(rotationDegree);
        try{
            mMediaRecorder.prepare();
            if(CAMERA_TYPE==Camera.CameraInfo.CAMERA_FACING_FRONT){
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            mMediaRecorder.start();
        }catch (Exception e){
            releaseMediaRecorder();
            return false;
        }
        return true;
    }


    private void releaseMediaRecorder() {
        //todo 释放MediaRecorder
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        mCamera.lock();
    }
}

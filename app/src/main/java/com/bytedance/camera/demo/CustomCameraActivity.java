package com.bytedance.camera.demo;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_IMAGE;
import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.bytedance.camera.demo.utils.Utils.getOutputMediaFile;

public class CustomCameraActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private Camera mCamera;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;

    private int rotationDegree = 0;

    private int cameraPosition = 1; //当前选用的摄像头，1后置 0前置

    private int flashMode = 1; //当前选用的闪光灯模式

    private Camera.AutoFocusCallback myAutoFocusCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);

        mSurfaceView = findViewById(R.id.img);
        //todo 给SurfaceHolder添加Callback
        mCamera = getCamera(CAMERA_TYPE);
        startPreview(mSurfaceView.getHolder());

        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            //todo 拍一张照片
            mCamera.takePicture(null,null,mPicture);
            Toast.makeText(CustomCameraActivity.this,"take a picture",Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btn_record).setOnClickListener(v -> {
            //todo 录制，第一次点击是start，第二次点击是stop
            if (isRecording) {
                //todo 停止录制
                releaseCameraAndPreview();
                isRecording = false;
                Toast.makeText(CustomCameraActivity.this,"record stop",Toast.LENGTH_SHORT).show();

            } else {
                //todo 录制
                isRecording = prepareVideoRecorder();
                Toast.makeText(CustomCameraActivity.this,"record start",Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_facing).setOnClickListener(v -> {
            //todo 切换前后摄像头
            switchFrontCamera();
        });

        findViewById(R.id.btn_zoom).setOnClickListener(v -> {
            //todo 调焦，需要判断手机是否支持
            try {
                mCamera.autoFocus(myAutoFocusCallback);
            }catch (Exception e){
                Toast.makeText(CustomCameraActivity.this, "zoom:fail",Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_flash).setOnClickListener(v -> {
            switchFlashMode();
        });
    }

    public void switchFlashMode(){
        Camera.Parameters mParameters = mCamera.getParameters();
        String mIsOpenFlashMode = Camera.Parameters.FLASH_MODE_ON;
        switch (flashMode){
            case 1:
                mIsOpenFlashMode = Camera.Parameters.FLASH_MODE_ON;//总是开启
                Toast.makeText(CustomCameraActivity.this, "flash mode: on", Toast.LENGTH_SHORT).show();
                flashMode = 2;
                break;
            case 2:
                mIsOpenFlashMode = Camera.Parameters.FLASH_MODE_AUTO;//自动模式
                Toast.makeText(CustomCameraActivity.this, "flash mode: auto", Toast.LENGTH_SHORT).show();
                flashMode = 3;
                break;
            case 3:
                mIsOpenFlashMode = Camera.Parameters.FLASH_MODE_OFF;//总是关闭
                Toast.makeText(CustomCameraActivity.this, "flash mode: off", Toast.LENGTH_SHORT).show();
                flashMode = 1;
                break;
            default:
                break;
        }
        //设置闪光灯模式
        mParameters.setFlashMode(mIsOpenFlashMode);
        mCamera.setParameters(mParameters);
    }

    public void switchFrontCamera() {
        int cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (cameraPosition == 1) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    //重新打开
                    reStartCamera(i);
                    cameraPosition = 0;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    reStartCamera(i);
                    cameraPosition = 1;
                    break;
                }
            }
        }
    }

    public void reStartCamera(int position) {
        if (mCamera != null) {
            mCamera.stopPreview();//停掉原来摄像头的预览
            mCamera.release();//释放资源
            mCamera = null;//取消原来摄像头
        }
        try {
            mCamera = Camera.open(position);//打开当前选中的摄像头
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());//通过surfaceview显示取景画面

            int degree = 0;
            if(cameraPosition == 1){
                degree = (getCameraDisplayOrientation(cameraPosition) + 180) % 360;
            }else {
                degree = getCameraDisplayOrientation(cameraPosition);
            }
            mCamera.setDisplayOrientation(degree);// 屏幕方向
            mCamera.startPreview();//开始预览
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            //success表示对焦成功
            if (success){
                Toast.makeText(CustomCameraActivity.this,"AutoFocus:success",Toast.LENGTH_SHORT).show();
                Log.i("TAG", "myAutoFocusCallback:success...");
                //myCamera.setOneShotPreviewCallback(null);
            } else {
                //未对焦成功
                Toast.makeText(CustomCameraActivity.this,"AutoFocus:fail",Toast.LENGTH_SHORT).show();
                Log.i("TAG", "myAutoFocusCallback: fail...");
            }
        }
    };

    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);

        //todo 摄像头添加属性，例是否自动对焦，设置旋转方向等

        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
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
        releaseMediaRecorder();
        startPreview(mSurfaceView.getHolder());

    }

    Camera.Size size;

    private void startPreview(SurfaceHolder surfaceHolder) {
        //todo 开始预览
        surfaceHolder.setType(surfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    mCamera.setPreviewDisplay(surfaceHolder);
                    mCamera.setDisplayOrientation(getCameraDisplayOrientation(CAMERA_TYPE));
                    mCamera.startPreview();
                }catch (Exception e){
                    Toast.makeText(CustomCameraActivity.this,"error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        });
    }


    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder() {
        //todo 准备MediaRecorder
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));

        mMediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        mMediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
        mMediaRecorder.setOrientationHint(rotationDegree);

        try{
            mMediaRecorder.prepare();
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


    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }

        mCamera.startPreview();
    };


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}

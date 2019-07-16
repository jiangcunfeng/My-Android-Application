package cn.edu.zju.myapplicationhomework7;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class ViewImagesActivity extends AppCompatActivity {
    private ViewPager pager = null;
    private LayoutInflater layoutInflater = null;
    private List<View> pages = new ArrayList<View>();
    private ViewAdapter adapter;

    private String[] mPermissionsArrays = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private final static int REQUEST_PERMISSION = 123;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_images);

        layoutInflater = getLayoutInflater();
        pager = findViewById(R.id.viewPager);

        pager.post(new Runnable() {
            @Override
            public void run() {
                new ReadFileByGlide("https://cdn.pixabay.com/photo/2019/07/13/16/47/skyline-4335245__480.jpg");
                new ReadFileByGlide("https://cdn.pixabay.com/photo/2019/07/08/19/17/hot-air-balloon-4325398__480.jpg");
                new ReadFileByGlide("https://cdn.pixabay.com/photo/2019/07/13/15/27/scotland-4335030__480.jpg");

                boolean getPermission = checkPermissionAllGranted(mPermissionsArrays);
                if (!getPermission) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(mPermissionsArrays, REQUEST_PERMISSION);
                    }
                }else {
                    getLocalImage();
                }

                adapter = new ViewAdapter();
                pager.setAdapter(adapter);

                adapter.refresh(pages);
            }
        });

    }

    private class ReadFileByGlide {
        ImageView imageView;

        ReadFileByGlide(String path) {
            imageView = (ImageView) layoutInflater.inflate(R.layout.activity_image_item, null);
            Glide.with(ViewImagesActivity.this)
                    .load(path)
                    .error(R.drawable.error)
                    .into(imageView);
            pages.add(imageView);
        }
    }

    private void getLocalImage(){
        new ReadFileByGlide("/sdcard/Download/pic.jpg");
        new ReadFileByGlide("/sdcard/Download/pic2.jpg");
        new ReadFileByGlide("/sdcard/Download/pic3.jpg");
        new ReadFileByGlide("/sdcard/Download/pic4.jpg");
        new ReadFileByGlide("/sdcard/Download/pic5.jpg");
    }

    private boolean checkPermissionAllGranted(String[] permissions) {
        // 6.0以下不需要
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            int permittedNum = 0;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults.length > i && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "已经授权" + permissions[i], Toast.LENGTH_LONG).show();
                    permittedNum++;
                }
            }
            if(permittedNum == permissions.length){
                getLocalImage();
                adapter.refresh(pages);
            }
        }
    }
}

package cn.edu.zju.myapplicationhomework7;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goGetPermission(View view){
        Intent intent = new Intent(this, GetPermissionActivity.class);
        startActivity(intent);
    }

    public void goViewImages(View view){
        Intent intent = new Intent(this, ViewImagesActivity.class);
        startActivity(intent);
    }

    public void goVideoPlayer(View view){
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        startActivity(intent);
    }
}

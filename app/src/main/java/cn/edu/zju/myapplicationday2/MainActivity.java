package cn.edu.zju.myapplicationday2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private String[] hotMsgTitle = {
            "表面光鲜亮丽其实穿着大裤衩",
            "网友变身吴亦凡",
            "朱广权被手语翻译老师吐槽",
            "中国仪仗队女兵惊艳全场",
            "郭艾伦晃到对手",
            "朱广权批夏天炫腹的膀爷",
            "大爷男团请求出道",
            "彭于晏给洪金宝敬茶磕头",
            "长沙橘子洲被淹",
            "在床上玩手机最舒服的时候",
            "300斤女孩直播哗众取宠被抓",
            "肖战神仙颜值",
            "当你跟你妈要零花钱的时候",
            "杨紫李现 甜",
            "明星高能口误合集",
            "消防员救出小孩子后哭了",
            "心疼瓢哥",
            "刘德华拍戏用真刀",
            "王凯王鸥牵手",
            "鹿晗 我没驼背",
            "邓紫棋把瓶盖踢回去",
            "终于找到了理想的暑假工",
            "特警踢瓶盖太帅了",
            "城管抱住小贩抢气球放飞",
            "沉睡魔咒2预告",
            "萍乡暴雨",
            "向佐接机郭碧婷郭爸爸",
            "柯基的基因有多强大",
            "女子史诗级碰瓷惊呆路人",
            "来自亲爸的嫌弃"
    };

    private double[] hotMsgValue = {
            1024.0,
            844.0,
            595.8,
            580.7,
            428.1,
            418.3,
            338.8,
            201.2,
            153.2,
            150.1,
            141.5,
            120.6,
            106.1,
            94.3,
            86.9,
            82.4,
            80.5,
            71.8,
            66.0,
            50.2,
            45.6,
            41.6,
            38.7,
            37.9,
            37.0,
            32.7,
            31.9,
            31.2,
            30.4,
            29.7
    };

    private List<HotMessage> hotMsgList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///初始化
        init();
        //获取RecyclerView的实例
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        //创建一个LayoutManager，这里使用LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //完成LayoutManager设置
        recyclerView.setLayoutManager(layoutManager);
        //创建HotMessageAdapter的实例同时将hotMessageList传入其构造函数
        HotMessageAdapter adapter = new HotMessageAdapter(hotMsgList);
        //完成Adapter设置
        recyclerView.setAdapter(adapter);
    }

    public void backMyPage(View view) {
        Intent intent = new Intent(this, MyPageActivity.class);
        startActivity(intent);
    }

    private void init() {
        for (int i = 0; i < 30; i++) {
            HotMessage msg = new HotMessage("" + (i + 1) + ".", hotMsgTitle[i], "" + hotMsgValue[i] + "w");
            hotMsgList.add(msg);
        }
    }
}

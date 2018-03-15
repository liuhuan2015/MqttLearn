package com.liuh.mqttlearn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

/**
 * 这个是学习一下Mqtt的基本使用的一个项目.来自http://blog.csdn.net/qq_17250009/article/details/52774472
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, MyMqttService.class));

        findViewById(R.id.btn_publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMqttService.publish("测试之山穷水尽疑无路");
            }
        });
    }
}

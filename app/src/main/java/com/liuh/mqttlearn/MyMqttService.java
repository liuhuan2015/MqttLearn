package com.liuh.mqttlearn;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 * Date: 2018/3/14 16:30
 * Description:MQTT长连接服务
 */

public class MyMqttService extends Service {

    public static final String TAG = MyMqttService.class.getSimpleName();

    private static MqttAndroidClient client;

    private MqttConnectOptions connectOptions;

    private String host = "tcp://192.168.1.40:61613";

    private String username = "admin";

    private String password = "password";

    private static String myTopic = "topic";

    private String clientId = "test";


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        init();
        return super.onStartCommand(intent, flags, startId);
    }

    private void init() {
        //服务器地址(协议+地址+端口号)
        String uri = host;
        client = new MqttAndroidClient(this, uri, clientId);
        //设置mqtt监听并接受消息
        client.setCallback(mqttCallBack);

        connectOptions = new MqttConnectOptions();
        //清除缓存
        connectOptions.setCleanSession(true);
        //设置超时时间,单位是秒
        connectOptions.setConnectionTimeout(10);
        //心跳包发送间隔,单位是秒
        connectOptions.setKeepAliveInterval(20);

        connectOptions.setUserName(username);

        connectOptions.setPassword(password.toCharArray());

        boolean doConnect = true;

        String message = "\"terminal_uid\":\"" + clientId + "\"}";

        String topic = myTopic;

        Integer qos = 0;

        boolean retained = false;

        if ((!message.equals("")) || (!topic.equals(""))) {
            //最后的遗嘱
            connectOptions.setWill(topic, message.getBytes(), qos.intValue(), retained);
        }

        if (doConnect) {
            doClientConnection();
        }

    }

    /**
     * 连接mqtt服务器
     */
    private void doClientConnection() {
        if (!client.isConnected() && isConnectIsNormal()) {
            try {
                client.connect(connectOptions, null, iMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 判断网络是否连接
     *
     * @return
     */
    private boolean isConnectIsNormal() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            Log.e("----------", "MQTT 当前网络名称:" + name);
            return true;
        } else {
            Log.e("----------", "MQTT 当前没有可用网络");
            return false;
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void publish(String msg) {
        String topic = myTopic;
        Integer qos = 0;
        Boolean retained = false;

        try {
            client.publish(topic, msg.getBytes(), qos.intValue(), retained.booleanValue());
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    private MqttCallback mqttCallBack = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.e("--------------", "connectionLost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            Log.e("--------------", "messageArrived");
            String str1 = new String(message.getPayload());

            String str2 = topic + "--qos:" + message.getQos() + ",retained:" + message.isRetained();

            Log.e("--------------", "message.getPayload():" + new String(message.getPayload()));
            Log.e("--------------", "str2:" + str2);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.e("--------------", "deliveryComplete");
        }
    };

    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.e("--------------", "连接成功");
            try {
                //订阅myTopic话题
                client.subscribe(myTopic, 1);
            } catch (MqttException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            exception.printStackTrace();
            Log.e("--------------", "连接失败: " + exception.getMessage());

        }
    };

    @Override
    public void onDestroy() {

        try {
            if (client != null) {
                client.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}

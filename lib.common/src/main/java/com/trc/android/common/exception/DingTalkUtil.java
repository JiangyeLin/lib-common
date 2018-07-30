package com.trc.android.common.exception;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by wangqing on 2018/2/23.
 */

/**
 * 使用方法：直接调用sengMsg方法，传递三个参数
 * urlPath：设置钉钉机器人的群的请求地址，可以自己在任何顶顶群里设置（必填）。例：
 * private String urlPath = "https://oapi.dingtalk.com/robot/send?access_token=455c76d858fd025a3f94464f9fc6743b918d81c44fd8e3d70cc4adc75d6ddc35";
 * content：要发送的消息的内容（必填）
 * phoneList：发送钉钉消息要@的人的手机号码，只需要手机号码，不需要@符号（选填）
 */
public class DingTalkUtil {

    public static void sendMsg(final String urlPath, final String content, final List<String> phoneList, final boolean isAtAll) {
        new Thread(() -> request(urlPath, content, phoneList, isAtAll)).start();
    }

    public static void request(String urlPath, String content, List<String> phoneList, boolean isAtAll) {
        try {
            NailRobotModel nailRobotModel = new NailRobotModel();
            nailRobotModel.msgtype = "text";
            if (!TextUtils.isEmpty(content)) {
                NailRobotModel.TextBean textBean = new NailRobotModel.TextBean();
                textBean.content = content;
                nailRobotModel.text = textBean;
            } else {
                return;
            }
            if (!isAtAll) {
                if (phoneList != null) {
                    NailRobotModel.AtBean atBean = new NailRobotModel.AtBean();
                    atBean.atMobiles = phoneList;
                    nailRobotModel.at = atBean;
                }
            }
            nailRobotModel.isAtAll = isAtAll;
            String json = new Gson().toJson(nailRobotModel);
            URL url = new URL(urlPath);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Charset", "UTF-8");
            // 设置文件类型:
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            // 设置接收类型否则返回415错误
            //conn.setRequestProperty("accept","*/*")此处为暴力方法设置接受所有类型，以此来防范返回415;
            conn.setRequestProperty("accept", "application/json");
            // 往服务器里面发送数据
            if (json != null && !TextUtils.isEmpty(json)) {
                byte[] writebytes = json.getBytes();
                // 设置文件长度
                conn.setRequestProperty("Content-Length", String.valueOf(writebytes.length));
                OutputStream outwritestream = conn.getOutputStream();
                outwritestream.write(json.getBytes());
                outwritestream.flush();
                outwritestream.close();
                Log.d("hlhupload", "doJsonPost: conn" + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return;
        }
    }

    public static class NailRobotModel {
        /**
         * msgtype : text
         * text : {"content":"我就是我, 是不一样的烟火"}
         * at : {"atMobiles":["156xxxx8827","189xxxx8325"],"isAtAll":false}
         */

        @SerializedName("msgtype")
        public String msgtype;
        @SerializedName("text")
        public TextBean text;
        @SerializedName("at")
        public AtBean at;
        @SerializedName("isAtAll")
        private boolean isAtAll;

        public static class TextBean {
            /**
             * content : 我就是我, 是不一样的烟火
             */
            @SerializedName("content")
            public String content;
        }

        public static class AtBean {
            /**
             * atMobiles : ["156xxxx8827","189xxxx8325"]
             * isAtAll : false
             */

            @SerializedName("isAtAll")
            public boolean isAtAll;
            @SerializedName("atMobiles")
            public List<String> atMobiles;
        }
    }


}

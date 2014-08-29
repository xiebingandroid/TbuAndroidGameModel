package com.android.model;

import android.os.Environment;

public class PoxiaoConstants {

    public final static String FIRST_USE = "first_use";

    // 保存服务端返回的图片地址KEY
    public static final String IMAGE_SERVER_KEY = "com.hifreshday.doudizhudemo.IMAGE_SERVER_KEY";

    public static final String POXIAO_ROOT_DIR = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/.poxiaogame/";
    // 初始化音乐
    public static final String MUSIC_INIT = "com.hifreshday.singlegobang.activity.BaseActivity.BaseActivity.MUSIC_INIT";
    // 背景音乐禁止
    public static final String MUSIC_DISABLED = "com.hifreshday.singlegobang.activity.BaseActivity.BaseActivity.MUSIC_DISABLED";
    // 背景音乐启动
    public static final String MUSIC_ENABLED = "com.hifreshday.singlegobang.activity.BaseActivity.BaseActivity.MUSIC_ENABLED";
    // 背景音乐播放
    public static final String MUSIC_PLAY = "com.hifreshday.singlegobang.activity.BaseActivity.BaseActivity.MUSIC_PLAY";
    // 销毁自己的广播
    public static final String ACTIVITY_DESTORY_SELF_ACTION = "com.hifreshday.doudizhudemo.DoudizhuConstants.ACTIVITY_DESTORY_SELF_ACTION";

}

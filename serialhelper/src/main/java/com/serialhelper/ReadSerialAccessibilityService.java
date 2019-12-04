package com.serialhelper;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by 徐仕海 on 19-8-27.
 */

public class ReadSerialAccessibilityService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.e("辅助功能", "onAccessibilityEvent," + event.toString());
    }

    @Override
    public void onInterrupt() {
        Log.e("辅助功能", "onInterrupt");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        Log.e("辅助功能", "onKeyEvent:" + event.getKeyCode());
        if (event.getKeyCode() == 24)
            return true;
        return super.onKeyEvent(event);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.e("辅助功能", "onServiceConnected");
        SerialHelper.simulateClickSerial(this);
    }


    @RequiresApi(api = 26)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("辅助功能", "onStartCommand  " + intent.getAction());
        if (intent.getAction().equals("com.simulateClickStatusView")) {
            simulateClickStatusView();
            return START_STICKY_COMPATIBILITY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.e("辅助功能", "onDestroy");
        super.onDestroy();
    }

    /**
     * 检查系统设置：是否开启辅助服务
     *
     * @param service 辅助服务
     */
    public static boolean isSettingOpen(Class service, Context cxt) {
        try {
            int enable = Settings.Secure.getInt(cxt.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0);
            if (enable != 1)
                return false;
            String services = Settings.Secure.getString(cxt.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (!TextUtils.isEmpty(services)) {
                TextUtils.SimpleStringSplitter split = new TextUtils.SimpleStringSplitter(':');
                split.setString(services);
                while (split.hasNext()) { // 遍历所有已开启的辅助服务名
                    if (split.next().equalsIgnoreCase(cxt.getPackageName() + "/" + service.getName()))
                        return true;
                }
            }
        } catch (Throwable e) {//若出现异常，则说明该手机设置被厂商篡改了,需要适配
            Log.e("辅助功能", "isSettingOpen: " + e.getMessage());
        }
        return false;
    }

    /**
     * 跳转到系统设置：开启辅助服务
     */
    public static void jumpToSetting(final Context cxt) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            cxt.startActivity(intent);
            Log.e("辅助功能", "成功启动辅助功能界面");
        } catch (Throwable e) {//若出现异常，则说明该手机设置被厂商篡改了,需要适配
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void simulateClickStatusView() {
        Path path = new Path();
        int yStart = getResources().getDisplayMetrics().heightPixels;
        int yEnd = (int) (60 * getResources().getDisplayMetrics().density);
        int x = (int) (70 * getResources().getDisplayMetrics().density);
        path.moveTo(x, yStart);
        path.lineTo(x, yEnd);
        GestureDescription gestureDescription = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 200L, 400L, false))
                .build();

        dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);

                try {
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    if (nodeInfo != null && Build.VERSION.SDK_INT >= 18) {
                        int count = nodeInfo.getChildCount();
                        Log.e("View", "count:" + count);
                        ergodicNode(nodeInfo);

                        AccessibilityNodeInfo recyclerViewNode = nodeInfo.getChild(3);
                        ergodicNode(recyclerViewNode);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Handler());
        time = 0;
    }


    private void ergodicNode(AccessibilityNodeInfo nodeInfo) {
        int count = nodeInfo.getChildCount();

        if (count == 0) {
            parseSerialView(nodeInfo);
            Log.e("View", nodeInfo.toString());
            return;
        } else {
            Log.e("View", nodeInfo.toString());
        }
        for (int i = 0; i < count; i++) {
            ergodicNode(nodeInfo.getChild(i));
        }
    }

    String[] enterSerialSettings = {"型号和硬件", "状态消息"};
    String[] serialSettings = {"序列号"};

    private boolean contain(CharSequence text, String[] arr) {
        for (String settingView :
                arr) {
            if (settingView.equals(text))
                return true;
        }

        return false;
    }


    int time = 0;

    private boolean parseSerialView(AccessibilityNodeInfo nodeInfo) {
        boolean foundSerialView = false;
        if (contain(nodeInfo.getText(), enterSerialSettings)) {
            nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    ergodicNode(getRootInActiveWindow().getChild(3));
                    ergodicNode(getRootInActiveWindow());
                }
            }, 200);
            foundSerialView = true;
        } else if (contain(nodeInfo.getText(), serialSettings)) {
            final CharSequence serial = nodeInfo.getParent().getChild(1).getText();
            foundSerialView = true;
            if (++time == 1)
                SerialHelper.sendMessage(serial);
        }
        return foundSerialView;
    }



}

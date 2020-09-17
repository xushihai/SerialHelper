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
import android.widget.Toast;


/**
 * Created by 徐仕海 on 19-8-27.
 */

public class ReadSerialAccessibilityService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //Log.e("辅助功能", "onAccessibilityEvent," + event.toString());
    }

    @Override
    public void onInterrupt() {
       // Log.e("辅助功能", "onInterrupt");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
       // Log.e("辅助功能", "onKeyEvent:" + event.getKeyCode());
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
        if(intent==null){
            return super.onStartCommand(intent, flags, startId);
        }

        Log.e("辅助功能", "onStartCommand  " + intent.getAction());
        if (intent.getAction().equals("com.simulateClickStatusView")) {
            foundStatusView = false;
            foundStatusRetryTimes = 0;
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

    boolean foundStatusView = false;
    int foundStatusRetryTimes = 0;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void simulateClickStatusView() {
        Toast.makeText(this, "正在解析序列号，暂时切勿操作手机", Toast.LENGTH_LONG).show();
        Path path = new Path();
        int yStart = getResources().getDisplayMetrics().heightPixels - (int) (48 * getResources().getDisplayMetrics().density);
        int yEnd = (int) (60 * getResources().getDisplayMetrics().density);
        int x = (int) (70 * getResources().getDisplayMetrics().density);

        path.moveTo(x, yStart);
        path.lineTo(x, yEnd);


        GestureDescription gestureDescription = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 200L, 400L, false))
                .build();

        dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.e("辅助功能", "dispatchGesture onCancelled");
            }

            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Log.e("辅助功能", "dispatchGesture onCompleted");

                try {
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    if (nodeInfo != null && Build.VERSION.SDK_INT >= 18) {
                        ergodicNodePrint(nodeInfo);

                        AccessibilityNodeInfo recyclerViewNode = findRecyclerViewNode(nodeInfo);
                        if (recyclerViewNode != null)
                            ergodicNode(recyclerViewNode);
                        else
                            ergodicNode(nodeInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!foundStatusView) {
                    if (foundStatusRetryTimes++ <= 3) {
                        simulateClickStatusView();
                    } else {
                        Toast.makeText(ReadSerialAccessibilityService.this, "读取序列号失败", Toast.LENGTH_LONG).show();
                        SerialHelper.sendMessage(null);
                    }
                }
            }
        }, new Handler());
        time = 0;
    }


    private void ergodicNodePrint(AccessibilityNodeInfo nodeInfo) {
        int count = nodeInfo.getChildCount();
        if (count == 0) {
            Log.e("ViewGroup", "className:" + nodeInfo.getClassName() + ",text:" + nodeInfo.getText());
            return;
        } else {
            Log.e("ViewGroup", "className:" + nodeInfo.getClassName() + " childCount:" + count);
        }
        for (int i = 0; i < count; i++) {
            ergodicNodePrint(nodeInfo.getChild(i));
        }
    }

    private AccessibilityNodeInfo findRecyclerViewNode(AccessibilityNodeInfo rootNodeInfo) {
        int count = rootNodeInfo.getChildCount();
        for (int i = 0; i < count; i++) {
            if (rootNodeInfo.getChild(i).getClassName().toString().contains("RecyclerView")) {
                return rootNodeInfo.getChild(i);
            }
        }

        for (int i = 0; i < count; i++) {
            AccessibilityNodeInfo nodeInfo = findRecyclerViewNode(rootNodeInfo.getChild(i));
            if (nodeInfo != null) {
                return nodeInfo;
            }
        }
        return null;
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

    //中文简体，中文繁体，英文
    String[] enterSerialSettings = {"型号和硬件", "状态消息","Status","狀態訊息"};
    String[] serialSettings = {"序列号","Serial number","序號"};

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
                    AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
                    if (rootNodeInfo != null)
                        ergodicNode(getRootInActiveWindow());
                }
            }, 400);
//            }, 200);
            foundStatusView = true;
            foundSerialView = true;
        } else if (contain(nodeInfo.getText(), serialSettings)) {
            final CharSequence serial = nodeInfo.getParent().getChild(1).getText();
            foundSerialView = true;
            if (++time == 1)
                SerialHelper.sendMessage(serial);

            //读取完序列号后自动关闭该辅助功能，是为了避免影响有类似功能的辅助功能，如果这个功能不关闭，其他的类似的辅助功能即使开启也不能生效，会回调onCanceled,所以最好用完就给关闭掉
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                disableSelf();
            }
        }
        return foundSerialView;
    }


}

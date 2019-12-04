package com.serialhelper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;

import static com.serialhelper.SerialHelper.SerialHandler.MSG_OPEN_ACCESSIBILITY_SERVICE;
import static com.serialhelper.SerialHelper.SerialHandler.MSG_READ_SERIAL_OK;

/**
 * Created by 徐仕海 on 19-9-19.
 */

public class SerialHelper {

    public static SerialHandler handler;

    public static void getSerial(final Activity context, final OnReadSerialListener onReadSerialListener) {
        if (!Thread.currentThread().getName().equals("main")) {
            Log.e("SerialHelper", "当前线程：" + Thread.currentThread().getName() + " 不是主线程");
            return;
        }
        handler = new SerialHandler(context, onReadSerialListener);

        if (Build.VERSION.SDK_INT >= 29) {

        } else if (Build.VERSION.SDK_INT >= 26) {//Android8 android9使用Build.getSerial()
            SerialHelper.sendMessage(Build.getSerial());
            return;
        } else {//Android8.0之前使用Build.SERIAL
            SerialHelper.sendMessage(Build.SERIAL);
            return;
        }

        TextView messageTv = new TextView(context);
        messageTv.setText(R.string.open_accessibility_service_alert);
        messageTv.setTextColor(Color.parseColor("#757575"));
        messageTv.setTextSize(15);
        messageTv.setPadding(72, 30, 90, 30);
        messageTv.setLineSpacing(2f, 1.2f);
        new AlertDialog.Builder(context, R.style.AlertDialog)
                .setTitle("AndroidQ升级适配")
                .setView(messageTv)
                .setNegativeButton("退出", null)
                .setPositiveButton("前往打开", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        handler.sendEmptyMessageDelayed(MSG_OPEN_ACCESSIBILITY_SERVICE, 100);
                    }
                })
                .create()
                .show();

    }


    protected static void simulateClickSerial(final Context context) {
        if (handler == null)
            return;

        Intent intent = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
        context.startActivity(intent);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(context, ReadSerialAccessibilityService.class);
                i.setAction("com.simulateClickStatusView");
                context.startService(i);
            }
        }, 200);
    }

    protected static void sendMessage(CharSequence serialNumber) {
        if (handler == null)
            return;

        Message message = new Message();
        message.what = MSG_READ_SERIAL_OK;
        Bundle data = new Bundle();
        data.putCharSequence("serialNumber", serialNumber);
        message.setData(data);
        handler.sendMessageDelayed(message, 1000);
    }


    protected static class SerialHandler extends Handler {
        Activity context;
        OnReadSerialListener onReadSerialListener;
        public static final int MSG_OPEN_ACCESSIBILITY_SERVICE = 10;
        public static final int MSG_READ_SERIAL_OK = 11;

        public SerialHandler(Activity context, OnReadSerialListener onReadSerialListener) {
            this.context = context;
            this.onReadSerialListener = onReadSerialListener;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_OPEN_ACCESSIBILITY_SERVICE:
                    openAccessibilityService();
                    break;
                case MSG_READ_SERIAL_OK:
                    handleReadSerialOk(msg);
                    break;
            }
        }

        public void openAccessibilityService() {
            if (!ReadSerialAccessibilityService.isSettingOpen(ReadSerialAccessibilityService.class, context)) {
                ReadSerialAccessibilityService.jumpToSetting(context);
                Intent intent = new Intent(context, AlertOpenServiceDialog.class);
                context.startActivity(intent);
            } else {
                simulateClickSerial(context);
            }
        }

        public void handleReadSerialOk(Message msg) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            manager.moveTaskToFront(context.getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
            CharSequence serialNumber = msg.getData().getCharSequence("serialNumber");
            if (onReadSerialListener != null)
                onReadSerialListener.onSerialNumber(serialNumber);
        }
    }

    public static interface OnReadSerialListener {
        void onSerialNumber(CharSequence serialNumber);
    }
}

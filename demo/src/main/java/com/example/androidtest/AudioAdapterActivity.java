package com.example.androidtest;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.serialhelper.ReadSerialAccessibilityService;
import com.serialhelper.SerialHelper;

import java.io.ByteArrayOutputStream;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;


public class AudioAdapterActivity extends Activity {

    Switch wiredHeadsetIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        wiredHeadsetIndicator = findViewById(R.id.wired_headset_indicator);


        wiredHeadsetIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SerialHelper.getSerial2(AudioAdapterActivity.this, new SerialHelper.OnReadSerialListener() {
                    @Override
                    public void onSerialNumber(CharSequence serialNumber) {
                        Log.e("xxx", "serial:" + serialNumber);
                        Toast.makeText(AudioAdapterActivity.this, "序列号:" + serialNumber, Toast.LENGTH_SHORT).show();
//                        new AlertDialog.Builder(AudioAdapterActivity.this)
//                                .setTitle("读取序列号")
//                                .setMessage("序列号:" + serialNumber)
//                                .show();
                    }
                });
            }
        });

        RtpPacket rtpPacket = new RtpPacket();
        Log.e("rtp",Arrays.toString(rtpPacket.packet));
        Log.e("rtp","第一个字节："+   Integer.toBinaryString((rtpPacket.packet[0] & 0xFF) + 0x100).substring(1));
        Log.e("rtp","第一个字节："+   Integer.toBinaryString(10));
    }


}
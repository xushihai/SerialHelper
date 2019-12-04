package com.example.androidtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.serialhelper.SerialHelper;


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
                SerialHelper.getSerial(AudioAdapterActivity.this, new SerialHelper.OnReadSerialListener() {
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
    }


}
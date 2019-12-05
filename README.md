# AccessibilityService
[![](https://jitpack.io/v/xushihai/SerialHelper.svg)](https://jitpack.io/#xushihai/SerialHelper)

# 介绍

# 使用教程

```sh
 SerialHelper.getSerial(AudioAdapterActivity.this, new SerialHelper.OnReadSerialListener() {
                     @Override
                     public void onSerialNumber(CharSequence serialNumber) {
                         Log.e("xxx", "serial:" + serialNumber);
                     }
                 });
```

#注：
    调用SerialHelper.getSerial的activity的启动模式需要设置为singleInstance android:launchMode="singleInstance"
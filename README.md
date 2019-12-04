# AccessibilityService
[![](https://jitpack.io/v/xushihai/MediaPicker.svg)](https://jitpack.io/#xushihai/MediaPicker)

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

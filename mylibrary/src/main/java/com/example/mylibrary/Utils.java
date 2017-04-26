package com.example.mylibrary;

import android.os.Environment;

/**
 * Created by suhu on 2017/4/25.
 */

public class Utils {
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else {
            return false;
        }
    }
}

package com.midi_control.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ML {
    public static final Boolean GLOBAL_LOG_ENABLED = true;
    public static final Boolean GLOBAL_WARN_ENABLED = true;

    // classname ML = MY_LOG
    public static String[] log_tags = {"MY_LOG", "MY_ERR", "MY_WARN"};
    public static String[] log_local_tags = {"L", "E", "W"};
    // log_tags[] = ["MY_LOG", "MY_ERR", "MY_WARN"]
    public static List<String> log_history = new ArrayList<String>();
    private static Boolean useLogApi = true;


    public static Boolean isUsingLogApi() {
        return useLogApi;
    }
    public static void setUseLogApi(Boolean useLogApi) {
        ML.useLogApi = useLogApi;
        ML.log("ML", "ML.useLogApi = " + String.valueOf(useLogApi));
    }
    public static void clear_history(){
        log_history.clear();
        ML.warn("ML", "Log history cleared.");
    }


    public static void l(Object data){
        ML.log(log_local_tags[0], String.valueOf(data));
    }

    public static void e(Object data){
        ML.err(log_local_tags[1], String.valueOf(data));
    }

    public static void w(Object data){
        ML.warn(log_local_tags[2], String.valueOf(data));
    }

    public static void log(String tag, Object data){
        String line = "Log: [" + tag + "] --> " + String.valueOf(data);
        if (useLogApi && GLOBAL_LOG_ENABLED) Log.i(log_tags[0], line);
        log_history.add(line);
    }

    public static void err(String tag, Object data){
        String line = "Error: [" + tag + "] --> " + String.valueOf(data);
        log_history.add(line);
        if(useLogApi && GLOBAL_WARN_ENABLED) Log.e(log_tags[1], line);
    }

    public static void warn(String tag, Object data){
        String line = "Warning: [" + tag + "] --> " + String.valueOf(data);
        log_history.add(line);
        if(useLogApi) Log.w(log_tags[2], line);
    }
}

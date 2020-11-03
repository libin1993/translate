package com.doit.net.model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by wiker on 2016/4/28.
 */
public class PrefManage {

    private static SharedPreferences settings;
    private static SharedPreferences.Editor editor;
    public static void init(Context context){
        settings = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    public static boolean supportPlay = false;


    public static String getImsi(){
        return settings.getString("IMSI","");
    }

    public static void setImsi(String ip){
        editor.putString("IMSI",ip);
        editor.commit();
    }

    public static void setString(String key,String val){
        editor.putString(key,val);
        editor.commit();
    }

    public static String getString(String key,String defVal){
        return settings.getString(key,defVal);
    }

    public static void setInt(String key,int val){
        editor.putInt(key,val);
        editor.commit();
    }

    public static int getInt(String key, int defVal){
        return settings.getInt(key,defVal);
    }

    public static void setBoolean(String key, boolean val){
        editor.putBoolean(key,val);
        editor.commit();
    }

    public static boolean getBoolean(String key, boolean defVal){
        return settings.getBoolean(key,defVal);
    }

    public static Long getLong(String key,Long defVal){
        return settings.getLong(key,defVal);
    }

    public static void setLong(String key, long val){
        editor.putLong(key,val);
        editor.commit();
    }
}

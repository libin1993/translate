package com.doit.net.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Author：Li Bin on 2019/7/26 17:38
 * Description：
 */
public class GsonUtils {

    private static Gson gson;

    static {
        gson = new GsonBuilder().serializeNulls().create();
    }

    private GsonUtils() {

    }

    /**
     * 转成json
     *
     * @param object
     * @return
     */
    public static String objectToString(Object object) {
        String gsonString = null;
        if (gson != null) {
            gsonString = gson.toJson(object);
        }
        return gsonString;
    }


    /**
     * 转成json
     *
     * @param list
     * @return
     */
    public static<T> String listToString(List<T> list) {
        String gsonString = null;
        if (gson != null) {
            gsonString = gson.toJson(list);
        }
        return gsonString;
    }

    /**
     * 转成bean
     *
     * @param gsonString
     * @param cls
     * @return
     */
    public static <T> T jsonToBean(String gsonString, Class<T> cls) {
        T t = null;
        if (gson != null) {
            t = gson.fromJson(gsonString, cls);
        }
        return t;
    }

    /**
     * 转成list
     * 解决泛型问题
     *
     * @param json
     * @param cls
     * @param <T>
     * @return
     */

    public static <T> List<T> jsonToArray(String json, Class<T> cls) {
        List<T> list = new ArrayList<T>();
        if (gson != null) {
            JsonArray array = JsonParser.parseString(json).getAsJsonArray();
            for (JsonElement elem : array) {
                list.add(gson.fromJson(elem, cls));
            }
        }
        return list;
    }
}

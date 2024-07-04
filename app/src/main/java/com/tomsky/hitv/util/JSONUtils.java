
package com.tomsky.hitv.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JSONUtils<T> {
    private static final String TAG = JSONUtils.class.getSimpleName();
    public static Gson gson = new Gson();

    public static <T> T fromJson(Class<T> type, String source) {
        if(TextUtils.isEmpty(source)){
            return null;
        }
        try {
            T result = gson.fromJson(source, type);
            return result;
        } catch (Exception e) {
//            if (BuildConfig.DEBUG) {
                Log.e(TAG, e.toString());
//            }
        }
        return null;
    }

    public static <T> T fromJson(Class<T> type, JsonElement source) {
        try {
            T result = gson.fromJson(source.toString(), type);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Convert string to given type
     *
     * @param json
     * @param type
     * @return instance of type
     */
    public static final <T> T fromJsonType(String json, Type type) {
        return gson.fromJson(json, type);
    }

    /**
     * 解析静态字符串
     *
     * @param type
     * @param source
     * @param <T>
     * @return
     */
    public static <T> List<T> fromJsonArray(Class<T[]> type, String source) {
        try {
            T[] list = gson.fromJson(source, type);
            //备注：不能直接返回Arrays.asList(list)，此处需要包一层ArrayList转换。
            // 因为Arrays.asList(list)返回的是Arrays$ArrayList 非ArrayList对象，
            // 但两者都继承自AbstractList，而Arrays$ArrayList并没有实现add、remove、clear方法，
            // 后续使用的话会抛throw UnsupportedOperationException
            return new ArrayList<T>(Arrays.asList(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<T>();
    }

    public static <T> String toJson(T obj) {
        String result = gson.toJson(obj);
        return result;
    }

    public static Map<String, Integer> fromJsonMap(String source) {
        try {
            Gson myGson = new GsonBuilder()
                    .registerTypeAdapter(
                            new TypeToken<HashMap<String, Object>>(){}.getType(),
                            new JsonDeserializer<HashMap<String, Object>>() {
                                @Override
                                public HashMap<String, Object> deserialize(
                                        JsonElement json, Type typeOfT,
                                        JsonDeserializationContext context) throws JsonParseException {

                                    HashMap<String, Object> treeMap = new HashMap<>();
                                    JsonObject jsonObject = json.getAsJsonObject();
                                    Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                                    for (Map.Entry<String, JsonElement> entry : entrySet) {
                                        treeMap.put(entry.getKey(), entry.getValue());
                                    }
                                    return treeMap;
                                }
                            }).create();
            return myGson.fromJson(source, new TypeToken<HashMap<String, Integer>>(){}.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new HashMap<String,Integer>();
    }

}

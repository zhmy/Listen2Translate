package com.zmy.listen2translate.translate;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zmy.listen2translate.data.TranslateData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransApi {
    private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";
    private static final String TRANS_API_APPID = "20161104000031325";
    private static final String TRANS_API_SECURITYKEY = "ITG5PiwRy2MyTYdxCH0C";

    private String appid = TRANS_API_APPID;
    private String securityKey = TRANS_API_SECURITYKEY;

    public interface TranslateCallback {
        void onTranslate(List<TranslateData> translateDatas);
    }

    private TranslateCallback callback;

    public void setCallback(TranslateCallback callback) {
        this.callback = callback;
    }

    public void getTransResult(final String query, final String from, final String to) {
        final Map<String, String> transParams = buildParams(query, from, to);

        new AsyncTask<Void, List<TranslateData>, List<TranslateData>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                //loading
            }

            @Override
            protected void onPostExecute(List<TranslateData> translateDatas) {
                super.onPostExecute(translateDatas);
                if (callback != null) {
                    callback.onTranslate(translateDatas);
                }
            }

            @Override
            protected List<TranslateData> doInBackground(Void... params) {
                String result = HttpGet.get(TRANS_API_HOST, transParams);

                Log.e("zmy", "translate result = "+result);
                List<TranslateData> finalResult = new ArrayList<TranslateData>();
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    String jsonList = jsonObj.optString("trans_result");
                    Type type = new TypeToken<ArrayList<TranslateData>>() {}.getType();
                    Gson gson = new Gson();
                    finalResult = gson.fromJson(jsonList, type);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return finalResult;
            }
        }.execute();
    }




    private Map<String, String> buildParams(String query, String from, String to) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);

        params.put("appid", appid);

        // 随机数
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);

        // 签名
        String src = appid + query + salt + securityKey; // 加密前的原文
        params.put("sign", MD5.md5(src));

        return params;
    }

}

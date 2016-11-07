package com.zmy.listen2translate;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.VoiceRecognitionService;
import com.zmy.listen2translate.data.TranslateData;
import com.zmy.listen2translate.translate.TransApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecognitionListener {
    private SpeechRecognizer speechRecognizer;

    private TextView start, stop, content, content2, content3;
    private boolean hasStart;
    private SettingWindow settingWindow;
    private String[] translateParams = {"汉译英", "英译汉"};

    private String transFrom;
    private String transTo;
    private SharedPreferences sp;

    public static final String EN = "en";
    public static final String ZH = "zh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 创建识别器
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this,
                new ComponentName(this, VoiceRecognitionService.class));
        // 注册监听器
        speechRecognizer.setRecognitionListener(this);
        sp = PreferenceManager.getDefaultSharedPreferences(this);

        transFrom = sp.getString("translateFrom", "auto");
        transTo = sp.getString("translateTo", EN);

        initView();

//        settingWindow = new SettingWindow(this);
//        settingWindow.setData(translateParams);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_ch);
        if (EN.equals(transTo)) {
            item.setChecked(true);
        }

        MenuItem item2 = menu.findItem(R.id.action_en);
        if (ZH.equals(transTo)) {
            item2.setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_ch) {
            if (!EN.equals(transTo)) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("translateTo", EN);
                editor.apply();
            }

            item.setChecked(true);
            return true;
        } else if (id == R.id.action_en) {
            if (!ZH.equals(transTo)) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("translateTo", ZH);
                editor.apply();
            }

            item.setChecked(true);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {

        start = (TextView) findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startASR();
                hasStart = true;
            }
        });

        content = (TextView) findViewById(R.id.content);
        stop = (TextView) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasStart) {
                    reset();
                }
            }
        });

        content2 = (TextView) findViewById(R.id.content2);
        content3 = (TextView) findViewById(R.id.content3);

    }

    private void reset() {
        speechRecognizer.cancel();
        speechRecognizer.stopListening();
        resetBtn();
    }

    private void resetBtn() {
        start.setText("start");
        content.setText("");
        content2.setText("");
    }


    // 开始识别
    void startASR() {
        Intent intent = new Intent();
        bindParams(intent);
        speechRecognizer.startListening(intent);
        start.setText("preparing");
    }

    void bindParams(Intent intent) {
        // 设置识别参数
        if (EN.equals(transTo)) {
            intent.putExtra(Constant.EXTRA_LANGUAGE, "cmn-Hans-CN");
        } else {
            intent.putExtra(Constant.EXTRA_LANGUAGE, "en-GB");
        }


        intent.putExtra(Constant.EXTRA_NLU, "enable");

    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        //准备就绪
        Log.e("zmy", "准备就绪 onReadyForSpeech ---- " + params.toString());

        start.setText("ready");

    }

    @Override
    public void onBeginningOfSpeech() {
        //开始说话
        Log.e("zmy", "开始说话 onBeginningOfSpeech");

        start.setText("start speak");
        resetBtn();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //音量变化
//        Log.e("zmy", "音量变化 onRmsChanged ---- " + rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        //录音数据传出处理
        Log.e("zmy", "录音数据传出处理 onBufferReceived ---- " + buffer.toString());
    }

    @Override
    public void onEndOfSpeech() {
        //说话结束处理
        Log.e("zmy", "说话结束处理 onEndOfSpeech");

        start.setText("end speak");
    }

    @Override
    public void onError(int error) {
        //出错处理
        Log.e("zmy", "出错处理 onError ---- " + error);
        StringBuilder sb = new StringBuilder();
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                sb.append("音频问题");
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                sb.append("没有语音输入");
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                sb.append("其它客户端错误");
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                sb.append("权限不足");
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                sb.append("网络问题");
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                sb.append("没有匹配的识别结果");
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                sb.append("引擎忙");
                break;
            case SpeechRecognizer.ERROR_SERVER:
                sb.append("服务端错误");
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                sb.append("连接超时");
                break;
        }

        Toast.makeText(this, sb, Toast.LENGTH_SHORT).show();
        reset();
    }

    @Override
    public void onResults(Bundle results) {
        //最终结果
        Log.e("zmy", "最终结果 onResults ---- " + results.toString());
        String json_res = results.getString("origin_result");
        try {
            JSONObject jsonObj = new JSONObject(json_res);
            JSONObject contentObj = jsonObj.optJSONObject("content");
            if (contentObj != null) {
                JSONArray array = new JSONArray(contentObj.optString("item"));
                if (array != null) {
                    for (int i = 0; i < array.length(); i++) {
                        content.setText(array.optString(i) + "\n");
                    }
                }

                beginToTranslate();
            } else {
                Toast.makeText(this, "解释失败", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void beginToTranslate() {
        TransApi api = new TransApi();
        String query = content.getText().toString();
        api.setCallback(new TransApi.TranslateCallback() {
            @Override
            public void onTranslate(List<TranslateData> translateDatas) {
                if (translateDatas == null) {
                    return;
                }
                for (TranslateData data : translateDatas) {
                    content2.setText(data.getSrc() + "\n");
                    content3.setText(data.getDst() + "\n");
                }
            }
        });

        api.getTransResult(query, transFrom, transTo);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        // 临时结果处理
        Log.e("zmy", "临时结果处理 onPartialResults ---- " + partialResults.toString());

        ArrayList<String> nbest = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (String temp : nbest) {
            content.setText(temp);
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        // 处理事件回调
        Log.e("zmy", "处理事件回调 onEvent ---- " + eventType + " , " + params.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }
}

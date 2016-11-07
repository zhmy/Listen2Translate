package com.zmy.listen2translate;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import java.util.List;

/**
 * Created by zmy on 2016/11/4.
 */

public class SettingWindow extends PopupWindow {

    private Context context;
    private ListView listView;

    public SettingWindow(Context context) {
        super(context);
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.setting_layout, null);
        setContentView(view);
        listView = (ListView) view.findViewById(R.id.listview);
        setTouchable(true);
        setFocusable(true);
        setOutsideTouchable(true);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

    }

    public void setData(String[] data) {
        listView.setAdapter(new ArrayAdapter<String>(context,
                R.layout.setting_item, R.id.set_content, data));

    }
}

package com.drcuiyutao.annotation.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.drcuiyutao.annotation.R;
import com.drcuiyutao.lib.annotation.AnnotationUtil;
import com.drcuiyutao.lib.base.demo.BaseInsertUtil;

import java.util.List;


/**
 * 无埋点：测试注入点击事件
 */
public class TestClickEventActivity extends AppCompatActivity {

    public static final String TAG = "TestClickEventActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_click);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "click event in TestClickEventActivity.onCreate()");
            }
        });

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.i(TAG, "onCheckedChanged event TestClickEventActivity.onCreate()");
            }
        });

        ListView listView = findViewById(R.id.list_view);
        String[] strs = {"text1","text2","text3","text4","text5","text6"};
        listView.setAdapter(new ArrayAdapter<>(TestClickEventActivity.this, R.layout.item_text, R.id.text, strs));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick event TestClickEventActivity.onCreate()");
            }
        });

    }

    public static String testInsertMethod() {
        return "failed!";
    }

    public static String getTestText() {
        return "";
    }
}

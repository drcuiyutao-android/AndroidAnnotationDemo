package com.drcuiyutao.annotation.demo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.drcuiyutao.annotation.R;
import com.drcuiyutao.lib.annotation.AnnotationUtil;
import com.drcuiyutao.lib.base.demo.BaseInsertUtil;


public class TestTryCatchActivity extends AppCompatActivity {

    private static final String TAG = "TestTryCatchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_catch);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryCatchMethod();
            }
        });
    }

    private void tryCatchMethod() {
        Log.i(TAG, "TestTryCatchActivity before tryCatchMethod()");
        int i = 1 / 0;
        Log.i(TAG, "TestTryCatchActivity after tryCatchMethod()");
    }

}

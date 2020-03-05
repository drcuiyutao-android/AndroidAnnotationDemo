package com.drcuiyutao.annotation.demo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.drcuiyutao.annotation.R;
import com.drcuiyutao.lib.base.demo.BaseInsertUtil;
import com.drcuiyutao.lib.annotation.AnnotationUtil;


public class MainActivity extends AppCompatActivity {

    public static String TEST_STR = null;
    public static String TEST_INITIALED_STR = AnnotationUtil.init("original test content");
    public static boolean TEST_BOOLEAN = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //app项目引用base库
        //base库的Config无法被app项目引用，采取app库向base项目注入代码
        // 测试插入方法
        TextView testMethod = findViewById(R.id.text);
        testMethod.setText(BaseInsertUtil.getLibTestText());

        // 测试插入String类型
        TextView testString = findViewById(R.id.text2);
        testString.setText(BaseInsertUtil.LIB_TEST_STR);

        // 测试插入已初始化String类型
        TextView testStringInitial = findViewById(R.id.text3);
        testStringInitial.setText(BaseInsertUtil.LIB_TEST_INITIALED_STR);

        // 测试插入boolean类型
        TextView booleanTestView = findViewById(R.id.test_boolean);
        booleanTestView.setText(BaseInsertUtil.LIB_TEST_BOOLEAN ? "success" : "failed");

        // 测试插入method
        TextView methodTestView = findViewById(R.id.test_method);
        methodTestView.setText(BaseInsertUtil.testLibInsertMethod());


        //app项目向当前项目注入代码
        // 测试插入方法
        TextView testMethodToApp = findViewById(R.id.text_app2app);
        testMethodToApp.setText(getTestText());

        // 测试插入String类型
        TextView testStringToApp = findViewById(R.id.text2_app2app);
        testStringToApp.setText(TEST_STR);

        // 测试插入已初始化String类型
        TextView testStringInitialToApp = findViewById(R.id.text3_app2app);
        testStringInitialToApp.setText(TEST_INITIALED_STR);

        // 测试插入boolean类型
        TextView booleanTestViewToApp = findViewById(R.id.test_boolean_app2app);
        booleanTestViewToApp.setText(TEST_BOOLEAN ? "success" : "failed");

        // 测试插入method
        TextView methodTestViewToApp = findViewById(R.id.test_method_app2app);
        methodTestViewToApp.setText(testInsertMethod());

    }

    public static String testInsertMethod() {
        return "failed!";
    }

    public static String getTestText() {
        return "";
    }
}

package com.drcuiyutao.annotation.util;

import android.util.Log;

import com.drcuiyutao.annotation.demo.TestInsertActivity;
import com.drcuiyutao.lib.annotation.Insert;

/**
 * @author DCH <a href="mailto:chuanhao.dai@drcuiyutao.com">Contract me.</a>
 * @since 2019/12/27
 */
public class InsertAppUtil {

    @Insert(target = TestInsertActivity.class, name = "TEST_STR")
    public static final String TEST_STR = "success";

    @Insert(target = TestInsertActivity.class, name = "TEST_INITIALED_STR")
    public static final String TEST_INITIALED_STR = "success";

    @Insert(target = TestInsertActivity.class, name = "TEST_BOOLEAN")
    public static final boolean TEST_BOOLEAN = true;

    @Insert(target = TestInsertActivity.class, replace = true)
    public static String getTestText() {
        return "success";
    }

    @Insert(target = TestInsertActivity.class, replace = true)
    public static String testInsertMethod() {
        Log.i(InsertAppUtil.class.getSimpleName(), "InsertUtil: testInsertMethod()");
        return "success";
    }
}

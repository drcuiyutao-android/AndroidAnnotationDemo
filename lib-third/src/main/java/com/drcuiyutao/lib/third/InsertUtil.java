package com.drcuiyutao.lib.third;

import android.util.Log;

import com.drcuiyutao.lib.base.demo.BaseInsertUtil;
import com.drcuiyutao.lib.annotation.Insert;

/**
 *
 */
public class InsertUtil {

    @Insert(target = BaseInsertUtil.class, name = "LIB_TEST_STR")
    public static final String LIB_TEST_STR = "success";

    @Insert(target = BaseInsertUtil.class, name = "LIB_TEST_INITIALED_STR")
    public static final String LIB_TEST_INITIALED_STR = "success";

    @Insert(target = BaseInsertUtil.class, name = "LIB_TEST_BOOLEAN")
    public static final boolean LIB_TEST_BOOLEAN = true;

    @Insert(target = BaseInsertUtil.class, replace = true)
    public static String getLibTestText() {
        return "success";
    }

    @Insert(target = BaseInsertUtil.class, replace = true)
    public static String testLibInsertMethod() {
        Log.i(InsertUtil.class.getSimpleName(), "InsertUtil: testInsertMethod()");
        return "success";
    }

}

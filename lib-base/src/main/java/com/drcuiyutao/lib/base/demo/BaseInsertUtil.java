package com.drcuiyutao.lib.base.demo;


import com.drcuiyutao.lib.annotation.AnnotationUtil;

/**
 * @author DCH <a href="mailto:chuanhao.dai@drcuiyutao.com">Contract me.</a>
 * @since 2019/12/27
 */
public class BaseInsertUtil {

    public static final String LIB_TEST_STR = null;

    public static final String LIB_TEST_INITIALED_STR = AnnotationUtil.init("original test content");

    public static boolean LIB_TEST_BOOLEAN = AnnotationUtil.init(false);

    public static String getLibTestText() {
        return "failed";
    }

    public static String testLibInsertMethod() {
        return "";
    }

}

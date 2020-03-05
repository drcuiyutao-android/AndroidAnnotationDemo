package com.drcuiyutao.lib.annotation.gradle.plugin

public class GradleLogUtil {

    static void i(String tag, Object msg) {
        System.out.println(tag + ": " + msg);
    }

    static void e(String tag, Object msg) {
//        System.out.println(tag + ": err: " + msg);
        System.err.println(tag + ": " + msg);
    }
}
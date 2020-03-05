package com.drcuiyutao.lib.annotation.gradle.plugin

import javassist.ClassPath
import javassist.CtClass
import javassist.ClassPool
import javassist.CtPrimitiveType

class GradlePluginClassPool extends ClassPool {
    private static final String TAG = "GradlePluginClassPool"

    private ArrayList<ClassPath> mClsPathList = new ArrayList<>()

    void addClassPath(String path) {
        mClsPathList.add(appendClassPath(path))
    }

    void release() {
        Enumeration enumeration = classes.elements()
        while (enumeration.hasMoreElements()) {
            try {
                CtClass ctClass = (CtClass) enumeration.nextElement()
                if ((null != ctClass) && !(ctClass instanceof CtPrimitiveType)) {
//                    GradleLogUtil.i(TAG, "release ctClass[" + ctClass.getName() + "]")
                    ctClass.detach()
                }
            } catch (Throwable e) {
                e.printStackTrace()
            }
        }
        classes.clear()

        for (ClassPath classPath : mClsPathList) {
//            GradleLogUtil.i(TAG, "release classPath[" + classPath + "]")
            removeClassPath(classPath)
        }
        mClsPathList.clear()
    }
}

package com.drcuiyutao.lib.annotation.gradle.plugin

import javassist.CtClass
import javassist.CtMethod

class AnnotationTransformInfo {
    public static final int TYPE_METHOD = 0
    public static final int TYPE_FIELD = 1
    public static final int TYPE_INTERFACE = 2

    public String className = null
    public int type = 0
    public String name = null
    public CtClass[] methodParms = null
    public String beforeCode = null
    public String afterCode = null
    public String replaceCode = null
    public Object value = null
    public String addCatch = null
    public CtMethod src = null
    public String clsName = null

    boolean isMethod() {
        return (TYPE_METHOD == type)
    }

    boolean isField() {
        return (TYPE_FIELD == type)
    }

    boolean isInterface() {
        return (TYPE_INTERFACE == type)
    }

    @Override
    String toString() {
        return "[\n  className[" + className +
                "]\n    type[" + type +
                "]\n    name[" + name +
                "]\n    beforeCode[" + beforeCode +
                "]\n    afterCode[" + afterCode +
                "]\n    replaceCode[" + replaceCode +
                "]\n    value[" + value +
                "]\n    addCatch[" + addCatch + "]]"
    }
}
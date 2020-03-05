package com.drcuiyutao.lib.annotation.gradle.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class AnnotationGradlePlugin implements Plugin<Project> {

    private static final String TAG = AnnotationGradlePlugin.class.getSimpleName()

    void apply(Project project) {
        GradleLogUtil.i(TAG, "------------------开始----------------------")
        def android = project.extensions.getByType(AppExtension)
        //注册一个Transform
        def classTransform = new AnnotationClassTransform(project)
        android.registerTransform(classTransform)

        GradleLogUtil.i(TAG, "------------------结束----------------------->")
    }
}
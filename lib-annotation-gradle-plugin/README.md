#lib-annotation-gradle-plugin

插入注解gradle插件

## 使用方法
项目buidl.gradle中添加依赖。
```
apply plugin: "lib-annotation-gradle-plugin"
buildscript {
    repositories {
        maven { url "$maven_url_release" }
    }
    dependencies {
        classpath "com.drcuiyutao:lib-annotation-gradle-plugin:$lib_annotation_gradle_plugin_version"
    }
}
```

## 更新日志

### 1.1.1
- 更新时间：2019-11-22
- 带返回值的非静态方法注入时, 方法体为空问题修改

### 1.1.0
- 更新时间：2019-11-20
- 解决main-dex方法数超出问题

### 1.0.8
- 更新时间：2019-06-03
- 添加非静态方法、非基础类型数据插入支持。非静态方法暂只支持replace模式

### 1.0.6
- 更新时间：2019-01-24
- 支持给函数添加try catch

### 1.0.5
- 更新时间：2019-01-08
- 支持接口

### 1.0.4
- 更新时间：2018-09-17
- win兼容问题

### 1.0.2
- 更新时间：2018-09-03
- win兼容问题

### 1.0.1
- 更新时间：2018-08-31
- win兼容问题

### 1.0.0
- 更新时间：2018-08-27
- 创建项目


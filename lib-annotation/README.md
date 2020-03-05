#lib-annotation

插入注解

## 使用方法
项目buidl.gradle中添加依赖。
```
implementation project(':lib-annotation')
```

addCatch参数必须以return或throw结束，catch的Throwable，想打印异常信息，可以使用$e。比如
"{ $e.printStackTrace(); return; }"
"{ System.out.println($e); throw $e; }"


属性只支持int, boolean, long, float, double, String
注入属性必须为static
被注入属性如果为编译时常量（static final 赋值常量），则需要修改为运行时常量，可以使用AnnotationUtil.init方法

java 运行时常量、编译时常量、静态块执行顺序
https://www.cnblogs.com/grefr/p/6094871.html


## 更新日志
### 1.0.3
- 更新时间：2019-06-03
- 添加fieldClzName，支持非基础类型数据插入

### 1.0.1
- 更新时间：2019-01-24
- 版本号：2
- 添加addCatch，支持给函数添加try catch


### 1.0.0
- 更新时间：2018-09-03
- 版本号：1
- 创建项目


# 注入注解框架

## 开发功能目的

   * 以编写注解代码注入的方式实现模块间解耦，不必影响基类库，更好的进行业务上的隔离，方便业务分模块进行开发，提高开发效率，解决模块间耦合冗余的问题
   
   * 实现无埋点，异常处理等日志功能


## 资源库介绍：

   * lib-annotation library：注解基础库，里边定义了Insert自定义注解，用于代码中需要注入的地方, 目前注入的属性只支持int, boolean, long,
   float, double, String, 注入属性必须为static final

   * ib-annotation-gradle-plugin library：gradle编译插件，该插件的核心作用是在代码编译期扫描类文件中使用Insert注解的地方，然后执行相应
   的代码注入操作

   * android-plugin-master：idea插件源码（需再次编译jar文件导入项目），编写该插件的目的是在编辑器中直观的看到使用了Insert注解的位置，可
   直接点击跳转到定义该注解的代码位置


## demo项目介绍：

![avatar](https://github.com/drcuiyutao-android/AndroidAnnotationDemo/blob/master/pic/modules.png?raw=true)

![avatar](https://github.com/drcuiyutao-android/AndroidAnnotationDemo/blob/master/pic/app_structure.png?raw=true)

![avatar](https://github.com/drcuiyutao-android/AndroidAnnotationDemo/blob/master/pic/app2_structure.jpg?raw=true)

   如图所示，项目分为app，app2，lib-base基础库，lib-third三方库(向lib-base中注入代码)
   其中，app引用lib-base库和lib-third三方库，app2作为对照组，只引用lib-base库


## 使用方法
### 1.首先需要添加资源库的依赖，在项目buidl.gradle中添加。

有两种添加依赖的方式：一种是将lib-annotation和lib-annotation-gradle-plugin源码下载下来添加依赖
```
    // app项目的build.gradle中:
    apply plugin: "lib-annotation-gradle-plugin"
    
    buildscript {
        dependencies {
            classpath "org.javassist:javassist:$javassist_version" //$javassist_version为javassist版本号
            classpath "com.drcuiyutao:lib-annotation-gradle-plugin:$lib_annotation_gradle_plugin_version"//lib_annotation_gradle_plugin_version为最新注解库插件版本号
        }
    }
    
    dependencies {
        ...
        implementation project(':lib-annotation')
        implementation "com.drcuiyutao:lib-annotation:$lib_annotation_version" //lib_annotation_version为注解库版本号
        ...
    }
```

    另一种方式是直接依赖maven仓库，这种方式不需要下载源码库
```
    // app项目的build.gradle中:
    apply plugin: "lib-annotation-gradle-plugin"
    
    buildscript {
     repositories {
            jcenter()
            google()
        }
        dependencies {
            classpath "org.javassist:javassist:$javassist_version" //依赖javassist源码
            classpath "com.drcuiyutao:lib-annotation-gradle-plugin:$lib_annotation_gradle_plugin_version" //lib_annotation_gradle_plugin_version为最新注解库插件版本号
        }
    }
    
    dependencies {
        ...
        implementation "com.drcuiyutao:lib-annotation:$lib_annotation_version" //lib_annotation_version为最新注解库版本号
        ...
    }
    
    
```

### 2. 添加依赖库和插件库成功后，在要注入代码的函数或变量上添加Insert注解。

例如demo项目中，app和app2项目都引用了lib-base库中的相关常量或方法，分别注入MainActivity中成员变量值：
```
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
```

### 3. 添加无埋点代码。

```

public class StatisticUtil {

    public static final String TAG = "StatisticUtil";

    @Insert(target = View.OnClickListener.class)
    public static void onClick(View v) {
        Log.i(TAG, "onClick v[" + v + "]");
    }

    @Insert(target = DialogInterface.OnClickListener.class)
    public static void onClick(DialogInterface dialog, int which) {
        Log.i(TAG, "onClick dialog[" + dialog + "] which[" + which + "]");
    }

    @Insert(target = AdapterView.OnItemClickListener.class)
    public static void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "onItemClick parent[" + parent + "] view[" + view + "] position[" + position + "] id[" + id + "]");
    }

    @Insert(target = AdapterView.OnItemSelectedListener.class)
    public static void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "onItemSelected parent[" + parent + "] view[" + view + "] position[" + position + "] id[" + id + "]");
    }

    @Insert(target = ExpandableListView.OnGroupClickListener.class)
    public static boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        Log.i(TAG, "onGroupClick parent[" + parent + "] v[" + v + "] groupPosition[" + groupPosition + "] id[" + id + "]");
        return false;
    }

    @Insert(target = ExpandableListView.OnChildClickListener.class)
    public static boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        Log.i(TAG, "onChildClick parent[" + parent + "] v[" + v + "] groupPosition[" + groupPosition + "] childPosition[" + childPosition + "] id[" + id + "]");
        return false;
    }

    @Insert(target = RatingBar.OnRatingBarChangeListener.class)
    public static void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        Log.i(TAG, "onRatingChanged ratingBar[" + ratingBar + "] rating[" + rating + "] fromUser[" + fromUser + "]");
    }

    @Insert(target = SeekBar.OnSeekBarChangeListener.class)
    public static void onStopTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "onStopTrackingTouch seekBar[" + seekBar + "]");
    }

    @Insert(target = CompoundButton.OnCheckedChangeListener.class)
    public static void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.i(TAG, "onCheckedChanged buttonView[" + buttonView + "] isChecked[" + isChecked + "]");
    }

    @Insert(target = RadioGroup.OnCheckedChangeListener.class)
    public static void onCheckedChanged(RadioGroup group, int checkedId) {
        Log.i(TAG, "onCheckedChanged group[" + group + "] checkedId[" + checkedId + "]");
    }

}

```


### 4.编译，在Build日志中查看是否有注入失败的函数或变量。运行查看效果。
```
    ...
   > Task :app:transformClassesWithAnnotationClassTransformForDebug
   AnnotationClassTransform: ----------------进入transform了--------------
   AnnotationClassTransform: transform classPath[C:\develop\android\sdk\android-sdk-windows\platforms\android-29\android.jar]
   AnnotationClassTransform: transform dir src[C:\work\test\android-annotation-demo\app\build\intermediates\javac\debug\compileDebugJavaWithJavac\classes]
   AnnotationClassTransform: transform dir dest[C:\work\test\android-annotation-demo\app\build\intermediates\transforms\AnnotationClassTransform\debug\29]
   AnnotationClassTransform: findTransformInfo dir[C:\work\test\android-annotation-demo\app\build\intermediates\javac\debug\compileDebugJavaWithJavac\classes]
   ...
   AnnotationClassTransform: findTransformInfoInJar file[C:\work\test\android-annotation-demo\lib-base\build\intermediates\intermediate-jars\debug\classes.jar]
   AnnotationClassTransform: injectClass copyInfo[mSrc[C:\work\test\android-annotation-demo\app\build\intermediates\javac\debug\compileDebugJavaWithJavac\classes]
   mDes[C:\work\test\android-annotation-demo\app\build\intermediates\transforms\AnnotationClassTransform\debug\29]]
   AnnotationClassTransform: inject ctField[com.drcuiyutao.annotation.demo.MainActivity.TEST_STR:Ljava/lang/String;]
   AnnotationClassTransform: inject value fieldString[public static java.lang.String TEST_STR = new String("success");]
   AnnotationClassTransform: inject value FieldAccess name[TEST_STR]
   AnnotationClassTransform: inject ctField[com.drcuiyutao.annotation.demo.MainActivity.TEST_INITIALED_STR:Ljava/lang/String;]
   AnnotationClassTransform: inject value fieldString[public static java.lang.String TEST_INITIALED_STR = new String("success");]
   AnnotationClassTransform: inject value FieldAccess name[TEST_INITIALED_STR]
   AnnotationClassTransform: inject ctField[com.drcuiyutao.annotation.demo.MainActivity.TEST_BOOLEAN:Z]
   AnnotationClassTransform: inject value fieldString[public static boolean TEST_BOOLEAN = true;]
   AnnotationClassTransform: inject value FieldAccess name[TEST_BOOLEAN]
   AnnotationClassTransform: inject ctField[com.drcuiyutao.lib.base.demo.BaseInsertUtil.LIB_TEST_STR:Ljava/lang/String;]
   AnnotationClassTransform: inject value fieldString[public final static java.lang.String LIB_TEST_STR = new String("success");]
   AnnotationClassTransform: inject value FieldAccess name[LIB_TEST_STR]
   AnnotationClassTransform: inject ctField[com.drcuiyutao.lib.base.demo.BaseInsertUtil.LIB_TEST_INITIALED_STR:Ljava/lang/String;]
   AnnotationClassTransform: inject value fieldString[public final static java.lang.String LIB_TEST_INITIALED_STR = new String("success");]
   AnnotationClassTransform: inject value FieldAccess name[LIB_TEST_INITIALED_STR]
   AnnotationClassTransform: inject ctField[com.drcuiyutao.lib.base.demo.BaseInsertUtil.LIB_TEST_BOOLEAN:Z]
   AnnotationClassTransform: inject value fieldString[public static boolean LIB_TEST_BOOLEAN = true;]
   AnnotationClassTransform: inject value FieldAccess name[LIB_TEST_BOOLEAN]
   AnnotationClassTransform: transform mTransformInfoList remain[0]
   AnnotationClassTransform: --------------结束transform了----------------
    ...
```

### demo运行截图(测试机型：小米6)

![avatar](https://github.com/drcuiyutao-android/AndroidAnnotationDemo/blob/master/pic/app_home.jpg?raw=true)

app项目：

    无埋点：

![avatar](https://github.com/drcuiyutao-android/AndroidAnnotationDemo/blob/master/pic/test_statistic.png?raw=true)

    代码注入：

![avatar](https://github.com/drcuiyutao-android/AndroidAnnotationDemo/blob/master/pic/app.jpg?raw=true)

    异常捕获：

![avatar](https://github.com/drcuiyutao-android/AndroidAnnotationDemo/blob/master/pic/test_try_catch?raw=true)


app2项目：

![avatar](https://github.com/drcuiyutao-android/AndroidAnnotationDemo/blob/master/pic/app2.jpg?raw=true)

通过app与app2项目对比看到，app项目中MainActivity中成员变量和方法内容都被注入方式修改了，app2项目未引入lib-third模块，即未使用注入框架，其MainActivity中成员变量值均为默认值，  
lib-third库作为额外的第三方库，可以很方便的拿掉或者引入，这样就轻松的实现了模块间的解耦，不影响app和app2都直接引用lib-base库，也不需要对lib-base库做出修改。
app2项目如果也想实现app项目中效果，在不使用注解库的情况下必须要对lib-base库中的代码进行修改，而lib-base库是所有项目都在使用的基类库，一般情况下是不允许修改的，所以注解库轻而易举的解决了这个问题。


### demo下载体验：

[app使用注解项目](https://raw.githubusercontent.com/drcuiyutao-android/AndroidAnnotationDemo/master/apks/app-release.apk)

[app2未使用注解项目](https://raw.githubusercontent.com/drcuiyutao-android/AndroidAnnotationDemo/master/apks/app2-release.apk)



### Insert注解说明

![avatar](https://github.com/drcuiyutao-android/AndroidAnnotationDemo/blob/master/pic/insert_1.png?raw=true)


### 注意：
>属性只支持int, boolean, long, float, double, String
 注入属性必须为static
 被注入属性如果为编译时常量（static final 赋值常量），则需要修改为运行时常量，可以使用AnnotationUtil.init方法


## 实现原理
1.定义Insert注解。保存注入时需要的信息,详见Insert.java。

2.自定义gradle插件:
* AnnotationGradlePlugin：实现Plugin，重写apply方法，注册自定义的transform
* AnnotationClassTransform：继承Transform, 核心内容为重写transform方法

3.gradle插件中遍历所有class及jar文件，收集有Insert注解的函数及变量。

4.gradle插件中遍历所有class及jar文件，根据上一步收集到的Insert注解信息，注入代码。注入工具使用[javassist官网](http://www.javassist.org/)

5.检查是否有未注入函数或变量。如果有停止编译。


## 注意事项
1. 使用注解库和插件需要在编译期进行工作，可能会增加编译时长。
2. 常量带默认值的请使用AnnotationUtil.init(xxx)方法进行初始化，否则自定义的默认值会无效。如：
```
   public static String TEST_INITIALED_STR = AnnotationUtil.init("original test content");
```
3. 编译android-plugin-master会下载idea相关资源包，文件较大耗时较长，请耐心等待下载完成，否则无法成功编译

## License
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

package com.drcuiyutao.lib.annotation.gradle.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.drcuiyutao.lib.annotation.Insert
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtField
import javassist.CtMethod
import javassist.Modifier
import javassist.NotFoundException
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.FieldInfo
import javassist.bytecode.MethodInfo
import javassist.bytecode.annotation.Annotation
import javassist.bytecode.annotation.ClassMemberValue
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class AnnotationClassTransform extends Transform {
    private static final String TAG = "AnnotationClassTransform"

    private Project mProject
    private ArrayList<AnnotationTransformInfo> mTransformInfoList = new ArrayList<>()
    private ArrayList<CopyInfo> mDirList = new ArrayList<>()
    private ArrayList<CopyInfo> mJarList = new ArrayList<>()

    AnnotationClassTransform(Project p) {
        this.mProject = p
    }

    //transform的名称
    //transformClassesWithBaseClassTransformForDebug 运行时的名字
    //transformClassesWith + getName() + For + Debug或Release
    @Override
    String getName() {
        return "AnnotationClassTransform"
    }

    //需要处理的数据类型，有两种枚举类型
    //CLASSES和RESOURCES，CLASSES代表处理的java的class文件，RESOURCES代表要处理java的资源
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

//    指Transform要操作内容的范围，官方文档Scope有7种类型：
//
//    EXTERNAL_LIBRARIES        只有外部库
//    PROJECT                       只有项目内容
//    PROJECT_LOCAL_DEPS            只有项目的本地依赖(本地jar)
//    PROVIDED_ONLY                 只提供本地或远程依赖项
//    SUB_PROJECTS              只有子项目。
//    SUB_PROJECTS_LOCAL_DEPS   只有子项目的本地依赖项(本地jar)。
//    TESTED_CODE                   由当前变量(包括依赖项)测试的代码
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    //指明当前Transform是否支持增量编译
    @Override
    boolean isIncremental() {
        return false
    }

//    Transform中的核心方法，
//    inputs中是传过来的输入流，其中有两种格式，一种是jar包格式一种是目录格式。
//    outputProvider 获取到输出目录，最后将修改的文件复制到输出目录，这一步必须做不然编译会报错
    @Override
    void transform(Context context,
                   Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException, TransformException, InterruptedException {
        GradleLogUtil.e(TAG, "----------------进入transform了--------------")

        try {
            GradlePluginClassPool pool = new GradlePluginClassPool()
            //project.android.bootClasspath 加入，不然找不到android相关的所有类
            mProject.android.bootClasspath.each { classPath ->
                GradleLogUtil.i(TAG, "transform classPath[" + classPath + "]")
                pool.addClassPath(classPath.toString())
            }
//            GradleLogUtil.i(TAG, "transform mProject[" + mProject + "]")
//            GradleLogUtil.i(TAG, "transform android[" + mProject.android + "]")

//            mProject.android.applicationVariants.each { variant ->
//                variant.javaCompile.classpath.each { classPath ->
//                    GradleLogUtil.i(TAG, "transform applicationVariants classPath[" + classPath + "]")
//                    pool.addClassPath(classPath.toString())
//                }
//            }

            // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
            inputs.each { TransformInput input ->
                //遍历文件夹
                input.directoryInputs.each { DirectoryInput directoryInput ->
                    // 获取output目录
                    def dest = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                    dest.mkdirs()
                    GradleLogUtil.i(TAG, "transform dir src[" + directoryInput.file.absolutePath + "]")
                    GradleLogUtil.i(TAG, "transform dir dest[" + dest.absolutePath + "]")

                    pool.addClassPath(directoryInput.file.absolutePath)
                    mDirList.add(new CopyInfo(directoryInput.file.absolutePath, dest.absolutePath))
                }
                //遍历jar文件
                input.jarInputs.each { JarInput jarInput ->
                    // 重命名输出文件（同目录copyFile会冲突）
                    def jarName = jarInput.name
                    def md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
                    if (jarName.endsWith(".jar")) {
                        jarName = jarName.substring(0, jarName.length() - 4)
                    }
                    def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                    GradleLogUtil.i(TAG, "transform jar src[" + jarInput.file.absolutePath + "]")
//                    GradleLogUtil.i(TAG, "transform jar dest[" + dest.absolutePath + "]")
                    dest.getParentFile().mkdirs()
                    if (dest.exists()) {
                        dest.delete()
                    }
                    pool.addClassPath(jarInput.file.absolutePath)
                    mJarList.add(new CopyInfo(jarInput.file.absolutePath, dest.absolutePath))
                }
            }
            findTransformInfo(pool)
//            GradleLogUtil.i(TAG, "transform mTransformInfoList total[" + mTransformInfoList.size() + "]")
//            if (mTransformInfoList.size() > 0) {
//                GradleLogUtil.e(TAG, "************* 所有注入方法 **************")
//                for (AnnotationTransformInfo info : mTransformInfoList) {
//                    GradleLogUtil.i(TAG, "" + info)
//                }
//                GradleLogUtil.e(TAG, "************* 所有注入方法 **************")
//            }
            inject(pool)
            releaseAll(pool)
            GradleLogUtil.i(TAG, "transform mTransformInfoList remain[" + mTransformInfoList.size() + "]")
            if (mTransformInfoList.size() > 0) {
                GradleLogUtil.i(TAG, "************* 注入接口 **************")
                Iterator<AnnotationTransformInfo> iterator = mTransformInfoList.iterator()
                while (iterator.hasNext()) {
                    AnnotationTransformInfo info = iterator.next()
                    if (info.isInterface()) {
                        GradleLogUtil.i(TAG, "interface is ok! info[" + info + "]")
                        iterator.remove()
                    }
                }
                GradleLogUtil.i(TAG, "************* 注入接口 **************")
            }
            if (mTransformInfoList.size() > 0) {
                GradleLogUtil.e(TAG, "************* 有未注入方法 **************")
                for (AnnotationTransformInfo info : mTransformInfoList) {
                    GradleLogUtil.e(TAG, "" + info)
                }
                GradleLogUtil.e(TAG, "************* 有未注入方法 **************")
                // 有未注入的方法，停止编译
                onError(pool)
            }
            GradleLogUtil.e(TAG, "--------------结束transform了----------------")
            pool = null
        } catch (Throwable e) {
            e.printStackTrace()
            onError(pool)
        }
    }

    private static void onError(GradlePluginClassPool pool) {
        releaseAll(pool)
        throw new RuntimeException()
    }

    private static void releaseAll(GradlePluginClassPool pool) {
        pool.release()
    }

    private void findTransformInfo(GradlePluginClassPool pool) {
        mDirList.each { copyInfo ->
            findTransformInfo(pool, new File(copyInfo.mSrc))
        }
        mJarList.each { copyInfo ->
            findTransformInfo(pool, new File(copyInfo.mSrc))
        }
    }

    private void findTransformInfo(GradlePluginClassPool pool, File dir) {
        if (dir.isDirectory()) {
            GradleLogUtil.i(TAG, "findTransformInfo dir[" + dir.absolutePath + "]")
            dir.eachFileRecurse { File file ->
                if (file.name.endsWith(".jar")) {
                    findTransformInfoInJar(pool, file)
                } else if (file.name.endsWith(".class")) {
                    findTransformInfoInClass(pool, file)
                }
            }
        } else if (dir.name.endsWith(".jar")) {
            findTransformInfoInJar(pool, dir)
        } else if (dir.name.endsWith(".class")) {
            findTransformInfoInClass(pool, dir)
        }
    }

    private void findTransformInfoInClass(GradlePluginClassPool pool, File file) {
//        GradleLogUtil.i(TAG, "findTransformInfoInClass file[" + file + "]")
        try {
            InputStream inputStream = new FileInputStream(file)
            find(pool, inputStream)
            if (inputStream != null) {
                inputStream.close()
            }
        } catch (Throwable e) {
            e.printStackTrace()
            onError(pool)
        }
    }

    private void findTransformInfoInJar(GradlePluginClassPool pool, File file) {
        GradleLogUtil.i(TAG, "findTransformInfoInJar file[" + file + "]")
        try {
            ZipInputStream inputStream = new ZipInputStream(new FileInputStream(file))
            while (true) {
                ZipEntry nextEntry = inputStream.getNextEntry()
                if (nextEntry == null) {
                    break
                }
                if (!nextEntry.isDirectory() && nextEntry.getName().endsWith(".class")) {
                    find(pool, inputStream)
                }
            }
            if (inputStream != null) {
                inputStream.close()
            }
        } catch (Throwable e) {
            e.printStackTrace()
            onError(pool)
        }
    }

    private boolean find(GradlePluginClassPool pool, InputStream inputStream) {
        boolean result = false
        try {
            CtClass ctClass = null
            try {
                ctClass = pool.makeClass(inputStream)
            } catch (Throwable e) {
                e.printStackTrace()
            }
//            GradleLogUtil.i(TAG, "find ctClass[" + ctClass + "]")
            if (null != ctClass) {
//                GradleLogUtil.i(TAG, "find ctClass.name[" + ctClass.name + "]")
                CtField[] ctFields = ctClass.getDeclaredFields()
                for (CtField ctField : ctFields) {
                    if (ctField.hasAnnotation(Insert.class)) {
                        Insert annotation = ctField.getAnnotation(Insert.class)
//                        GradleLogUtil.i(TAG, "find field annotation[" + annotation + "]")
                        if (null != annotation) {
                            AnnotationTransformInfo info = new AnnotationTransformInfo()
                            String className = getAnnotationClassValue(ctField, Insert.class, "target")
                            info.type = AnnotationTransformInfo.TYPE_FIELD
                            if (annotation.targetInKotlinCompanion()) {
                                info.className = className + "\$Companion"
                            } else {
                                info.className = className
                            }
                            if (!emptyString(annotation.name())) {
                                info.name = annotation.name()
                            } else {
                                info.name = ctField.getName()
                            }

                            String fieldClzName = annotation.fieldClzName()
                            if (!emptyString(fieldClzName)) {
                                info.clsName = fieldClzName
                            }
                            info.value = ctField.getConstantValue()

//                            GradleLogUtil.i(TAG, "find field info[" + info + "]")
                            mTransformInfoList.add(info)
                        }
                    }
                }
                CtMethod[] ctMethods = ctClass.getDeclaredMethods()
                for (CtMethod ctMethod : ctMethods) {
                    if (ctMethod.hasAnnotation(Insert.class)) {
                        Insert annotation = ctMethod.getAnnotation(Insert.class)
//                        GradleLogUtil.i(TAG, "find method annotation[" + annotation + "]")
                        if (null != annotation) {
                            AnnotationTransformInfo info = new AnnotationTransformInfo()
                            String className = getAnnotationClassValue(ctMethod, Insert.class, "target")
                            if (annotation.targetInKotlinCompanion()) {
                                info.className = className + "\$Companion"
                            } else {
                                info.className = className
                            }
                            if (isInterfaceClass(pool, info.className)) {
                                GradleLogUtil.i(TAG, "find method interface info[" + info + "]")
                                info.type = AnnotationTransformInfo.TYPE_INTERFACE
                            } else {
                                info.type = AnnotationTransformInfo.TYPE_METHOD
                            }
                            if (!emptyString(annotation.name())) {
                                info.name = annotation.name()
                            } else {
                                info.name = ctMethod.getName()
                            }
                            info.methodParms = ctMethod.getParameterTypes()
                            String java
                            if (annotation.sorceInKotlinObject()) {
                                java = ctClass.getName() + ".INSTANCE." + ctMethod.getName() + "(\$\$);"
                            } else if (annotation.sorceInKotlinCompanion()) {
                                int len = ctClass.getName().length() - "\$Companion".length()
                                java = ctClass.getName().substring(0, len) + ".Companion." + ctMethod.getName() + "(\$\$);"
                            } else {
                                if (Modifier.isStatic(ctMethod.getModifiers())) {
                                    java = ctClass.getName() + "." + ctMethod.getName() + "(\$\$);"
                                }
                            }
                            if (annotation.replace()) {
                                CtClass returnType = ctMethod.getReturnType()
//                                GradleLogUtil.i(TAG, "find method returnType[" + returnType + "]")
                                if (CtClass.voidType != returnType) {
                                    java = "return " + java
                                }
                                info.replaceCode = java
                                if (!Modifier.isStatic(ctMethod.getModifiers())) {
                                    info.src = ctMethod
                                    info.replaceCode = ""
                                }
                            } else {
                                if (0 == annotation.position()) {
                                    info.beforeCode = java
                                } else if (1 == annotation.position()) {
                                    info.afterCode = java
                                }
                            }
                            info.addCatch = annotation.addCatch()
//                            GradleLogUtil.i(TAG, "find method info[" + info + "]")
                            mTransformInfoList.add(info)
                        }
                    }
                }
                result = true
                ctClass.detach()//释放
            }
        } catch (Throwable e) {
            e.printStackTrace()
            onError(pool)
        }
        return result
    }

    static boolean isInterfaceClass(GradlePluginClassPool pool, String className) {
        CtClass ctClass = pool.get(className)
        boolean result = ctClass.isInterface()
//        if (className.equals("android.view.View\$OnClickListener")) {
//            GradleLogUtil.i(TAG, "isInterfaceClass OnClickListener result[" + result + "]")
//        }
        ctClass.detach()
        return result
    }

    static String getAnnotationClassValue(CtMethod ctMethod, Class annotationClass, String member) {
        MethodInfo methodInfo = ctMethod.getMethodInfo()
//        GradleLogUtil.i(TAG, "getAnnotationClassValue methodInfo[" + methodInfo + "]")
        AnnotationsAttribute attribute = (AnnotationsAttribute) methodInfo.getAttribute(AnnotationsAttribute.visibleTag)
//        GradleLogUtil.i(TAG, "getAnnotationClassValue attribute[" + attribute + "]")
//        GradleLogUtil.i(TAG, "getAnnotationClassValue annotationClass[" + annotationClass.getName() + "]")
        Annotation annotation = attribute.getAnnotation(annotationClass.getName())
//        GradleLogUtil.i(TAG, "getAnnotationClassValue annotation[" + annotation + "]")
        return ((ClassMemberValue) annotation.getMemberValue(member)).getValue().toString()
    }

    static String getAnnotationClassValue(CtField ctField, Class annotationClass, String member) {
        FieldInfo info = ctField.getFieldInfo()
//        GradleLogUtil.i(TAG, "getAnnotationClassValue info[" + info + "]")
        AnnotationsAttribute attribute = (AnnotationsAttribute) info.getAttribute(AnnotationsAttribute.visibleTag)
//        GradleLogUtil.i(TAG, "getAnnotationClassValue attribute[" + attribute + "]")
//        GradleLogUtil.i(TAG, "getAnnotationClassValue annotationClass[" + annotationClass.getName() + "]")
        Annotation annotation = attribute.getAnnotation(annotationClass.getName())
//        GradleLogUtil.i(TAG, "getAnnotationClassValue annotation[" + annotation + "]")
        return ((ClassMemberValue) annotation.getMemberValue(member)).getValue().toString()
    }

    void inject(GradlePluginClassPool pool) {
        mDirList.each { copyInfo ->
//            GradleLogUtil.i(TAG, "inject copyInfo[" + copyInfo + "]")
            injectClass(pool, copyInfo)
        }
        mJarList.each { copyInfo ->
//            GradleLogUtil.i(TAG, "inject copyInfo[" + copyInfo + "]")
            injectJar(pool, copyInfo)
        }
    }

    void injectClass(GradlePluginClassPool pool, CopyInfo copyInfo) {
        GradleLogUtil.i(TAG, "injectClass copyInfo[" + copyInfo + "]")
        File dir = new File(copyInfo.mSrc)
        if (dir.isDirectory()) {
            dir.eachFileRecurse { File file ->
                if (file.name.endsWith(".class")) {
                    File tempFile = new File(copyInfo.mDes + file.absolutePath.substring(copyInfo.mSrc.length()))
                    tempFile.getParentFile().mkdirs()
//                    GradleLogUtil.i(TAG, "injectClass tempFile[" + tempFile.absolutePath + "]")
                    try {
                        OutputStream outputStream = new FileOutputStream(tempFile)
                        InputStream inputStream = new FileInputStream(file)
                        inject(pool, inputStream, outputStream)
                        if (inputStream != null) {
                            inputStream.close()
                        }
                        if (outputStream != null) {
                            outputStream.close()
                        }
                    } catch (Throwable e) {
                        e.printStackTrace()
                        onError(pool)
                    }
                }
            }
        }
    }

    void injectJar(GradlePluginClassPool pool, CopyInfo copyInfo) {
//        GradleLogUtil.i(TAG, "injectJar copyInfo[" + copyInfo + "]")
        File tempFile = new File(copyInfo.mDes)
        File file = new File(copyInfo.mSrc)
        try {
            ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(tempFile))
            ZipInputStream inputStream = new ZipInputStream(new FileInputStream(file))
            while (true) {
                ZipEntry nextEntry = inputStream.getNextEntry()
                if (nextEntry == null) {
                    break
                }
//                    GradleLogUtil.i(TAG, "injectJar nextEntry Name[" + nextEntry.getName() + "]")
                outputStream.putNextEntry(createZipEntry(nextEntry))
                if (nextEntry.isDirectory() || !nextEntry.getName().endsWith(".class")) {
                    IOUtils.copy(inputStream, outputStream)
                } else {
                    inject(pool, inputStream, outputStream)
                }
            }
            if (inputStream != null) {
                inputStream.close()
            }
            if (outputStream != null) {
                outputStream.close()
            }
        } catch (Throwable e) {
            e.printStackTrace()
            onError(pool)
        }
    }

    boolean inject(GradlePluginClassPool pool, InputStream inputStream, OutputStream outputStream) {
        boolean result = false
        try {
            CtClass ctClass = null
            try {
                ctClass = pool.makeClass(inputStream)
            } catch (Throwable e) {
                e.printStackTrace()
            }
//            GradleLogUtil.i(TAG, "inject ctClass[" + ctClass + "]")
            if (null != ctClass) {
//                GradleLogUtil.i(TAG, "inject ctClass.name[" + ctClass.name + "]")
                //解冻
                if (ctClass.isFrozen()) {
                    ctClass.defrost()
                }
                ArrayList<AnnotationTransformInfo> removes = new ArrayList<>()
                mTransformInfoList.each { AnnotationTransformInfo info ->
                    if (info.isInterface()) {
                        CtClass[] interfaces = null
                        try {
                            interfaces = ctClass.getInterfaces()
                        } catch (Throwable e) {
                        }
                        if (null != interfaces) {
                            for (CtClass inter : interfaces) {
                                if (inter.name.equals(info.className)) {
//                                    GradleLogUtil.i(TAG, "inject interface ctClass[" + ctClass + "]")
                                    if (injectMethod(pool, ctClass, info)) {
//                                        removes.add(info)
                                    }
                                }
                            }
                        }
                    } else {
                        if (ctClass.name.equals(info.className)) {
                            if (info.isMethod()) {
                                if (injectMethod(pool, ctClass, info)) {
                                    removes.add(info)
                                }
                            } else if (info.isField()) {
                                CtField ctField = null
                                try {
                                    ctField = ctClass.getField(info.name)
                                } catch (Throwable e) {
                                    e.printStackTrace()
                                }
                                GradleLogUtil.i(TAG, "inject ctField[" + ctField + "]")
                                if (null != ctField) {
                                    try {
                                        String fieldString = ""
                                        int modifiers = ctField.getModifiers()
                                        if (Modifier.isPublic(modifiers)) {
                                            fieldString += "public "
                                        } else if (Modifier.isProtected(modifiers)) {
                                            fieldString += "protected "
                                        } else if (Modifier.isPrivate(modifiers)) {
                                            fieldString += "private "
                                        }
                                        if (Modifier.isFinal(modifiers)) {
                                            fieldString += "final "
                                        }
                                        if (Modifier.isStatic(modifiers)) {
                                            fieldString += "static "
                                        }
                                        if (Modifier.isVolatile(modifiers)) {
                                            fieldString += "volatile "
                                        }
                                        if (Modifier.isTransient(modifiers)) {
                                            fieldString += "transient "
                                        }

                                        if (!emptyString(info.clsName)) {
                                            GradleLogUtil.i(TAG, "inject value info.clsName[" + info.clsName + "] info.name[" + info.name + "]")
                                            fieldString += info.clsName + " " + info.name + " = "
                                        } else {
                                            GradleLogUtil.i(TAG, "inject value ctField.getType().getName()[" + ctField.getType().getName() + "] info.name[" + info.name + "]")
                                            fieldString += ctField.getType().getName() + " " + info.name + " = "
                                        }

                                        if (info.value instanceof String) {
                                            fieldString += "new String(\"" + info.value + "\");"
                                        } else {
                                            fieldString += "" + info.value + ";"
                                        }
                                        GradleLogUtil.i(TAG, "inject value fieldString[" + fieldString + "]")
//                                    GradleLogUtil.i(TAG, "inject value class[" + info.value.class + "] value[" + info.value + "]")
                                        ctClass.removeField(ctField)
                                        CtConstructor constructor = ctClass.getClassInitializer()
                                        if (null != constructor) {
                                            constructor.instrument(new ExprEditor() {
                                                @Override
                                                void edit(FieldAccess f) {
                                                    if (info.name.equals(f.getFieldName())) {
                                                        GradleLogUtil.i(TAG, "inject value FieldAccess name[" + f.getFieldName() + "]")
                                                        f.replace("{}")
                                                    }
                                                }
                                            })
                                        }
                                        CtField newField = CtField.make(fieldString, ctClass)
                                        ctClass.addField(newField)
                                        removes.add(info)
                                    } catch (Throwable e) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                }
                if (removes.size() > 0) {
                    mTransformInfoList.removeAll(removes)
                }
//                GradleLogUtil.i(TAG, "inject packageName[" + ctClass.packageName + "]")
                IOUtils.write(ctClass.toBytecode(), outputStream)
                result = true
                ctClass.detach()//释放
            }
        } catch (Throwable e) {
            e.printStackTrace()
        }
        return result
    }

    private boolean injectMethod(GradlePluginClassPool pool, CtClass ctClass, AnnotationTransformInfo info) {
        boolean result = false
        CtMethod ctMethod = null
        try {
            ctMethod = ctClass.getDeclaredMethod(info.name, info.methodParms)
        } catch (NotFoundException e) {
            if (info.isInterface() && Modifier.isAbstract(ctClass.getModifiers())) {
                result = true
                GradleLogUtil.i(TAG, "injectMethod abstract ctClass[" + ctClass + "] info[" + info + "]")
            }
        } catch (Throwable e) {
            e.printStackTrace()
        }
//        GradleLogUtil.i(TAG, "injectMethod ctMethod[" + ctMethod + "] ctClass[" + ctClass + "]")
        if (null != ctMethod) {
            try {
                if (!emptyString(info.replaceCode)) {
                    ctMethod.setBody(info.replaceCode)
                } else {
                    if (info.src != null) {
                        ctMethod.setBody(info.src, null)
                    }
                    if (!emptyString(info.beforeCode)) {
                        ctMethod.insertBefore(info.beforeCode)
                    }
                    if (!emptyString(info.afterCode)) {
                        ctMethod.insertAfter(info.afterCode)
                    }
                }
                if (!emptyString(info.addCatch)) {
                    CtClass etype = pool.get(Throwable.class.name)
//                    GradleLogUtil.i(TAG, "injectMethod addCatch[" + info.addCatch + "] etype[" + etype + "]")
                    ctMethod.addCatch(info.addCatch, etype)
                    etype.detach()
                }
                result = true
            } catch (Throwable e) {
                e.printStackTrace()
            }
        } else {
            GradleLogUtil.i(TAG, "injectMethod ctMethod null name[" + info.name + "]ctClass[" + ctClass + "]")
        }
        return result
    }

    static boolean emptyString(String str) {
        return (null == str) || "" == str.trim()
    }

    private static ZipEntry createZipEntry(ZipEntry zipEntry) {
        ZipEntry zipEntry2 = new ZipEntry(zipEntry.getName())
        zipEntry2.setComment(zipEntry.getComment())
        zipEntry2.setExtra(zipEntry.getExtra())
        return zipEntry2
    }

    static class CopyInfo {
        String mSrc
        String mDes

        CopyInfo(String src, String des) {
            mSrc = src
            mDes = des
        }

        @Override
        String toString() {
            return "mSrc[" + mSrc + "]\nmDes[" + mDes + "]"
        }
    }
}


原文地址：https://www.apkdv.com/android-smart-app-update/

## 什么是增量更新
应用越做越大，可能只是一个小小的改动就需要重新下载安装整个APP，这种方式即增加了服务器的压力，又浪费了用户的流量，因此每当我们发布新版本的时候，一些用户升级并不是很积极，这就造成了新版本的升级率并不高。而google为了解决了这个问题，提出了Smart App Update，即增量更新（也叫做差分升级）。

增量更新的原理非常简单，简单的说就是`通过某种算法找出新版本和旧版本不一样的地方`（这个过程也叫做差分，某种。。别问我是那种 因为我也不知道。。），然后将不一样的地方抽取出来形成所谓的更新补丁（patch）。客户端在检测到更新的时候，只需要下载差分包到本地，然后将差分包合并至本地的安装包，形成新版本的安装包，文件校验通过后再执行安装即可。本地的安装包通过提取当前已安装应用的apk得到。

## 演示：差分包的生成与合并
如下图所示(再次为我的艺术细胞所震撼)： 
![](https://dn-lengyue.qbox.me/image/2/38/5147c03fcd52bd67c5bad0970062c.png)

现在的问题在于如何生成差分包以及合并差分包。这里，我们借助开源库[bsdiff](http://www.pokorra.de/coding/bsdiff.html)来解决以上两个问题。首先我们先演示一下差分包的形成与合并。
我们先打出一个安装包，假设为old.apk。对源码做修改后，再打出一个新的安装包new.apk。此处old.apk相当于老版本的应用，而new.apk相当于新版本的应用。接下来，我们利用bsdiff来生成差分包patch.patch。

## 生成差分包
将上面的old.apk和new.apk放入bsdiff解压后的目录，然后在控制台中执行命令bsdiff old.apk new.apk patch.patch,稍等一会便可以生成差分包patch.patch，如下
![](https://dn-lengyue.qbox.me/image/5/96/59222caf19fa1d4cd9c4c9f039277.png)

使用命令`bsdiff old.apk new.apk update.patch` 便可以生成一个名为update.patch的补丁。

## 合并差分包
合并old.apk和patch.patch，生成新的安装包new.apk。只要此处合并出来的new.apk和上面我们自己打出来的new.apk(MD5)一样，那么就可以认为它就是我们需要的新版本安装包。

将old.apk和patch.patch放入bsdiff文件夹，执行命令：
`bspatch old.apk new.apk update.patch`，稍等一会之后便可以看到合并出的new.apk.
我们可以安装测试APP是否正常。

## 让自己的APP支持增量更新
客户端支持增量更新总体和上面的演示差不多，唯一的区别在于客户端要自行编译bspatch.c来实现合并差分包，也就是所谓的ndk开发，这里我们首先要下载[bsdiff](http://www.pokorra.de/coding/bsdiff.html)的源码以及[bszip](http://www.bzip.org/downloads.html)的源码。  
将bzip中除了.c .h 外的文件全部删除，将整个文件夹复制到AS的JNI目录，同时将bsdiff的源码复制到JNI目录，将bsdiff的代码打开将main方法改成`updatePatch`
目录结构应该是这样的：
![](https://dn-lengyue.qbox.me/image/2/b1/27b2742aa5b03de5f4366f44c4f38.png)

bspatch_util其实就是对接收到的Java参数的接收
```java
#include "com_lengyue_update_PatchUpdateUtil.h"


JNIEXPORT jint JNICALL Java_com_lengyue_update_PatchUpdateUtil(JNIEnv *env, jclass jclass1,
                                                               jstring old, jstring new,
                                                               jstring patch){
    char *argv[4];
    argv[0] = "bspatch";
    argv[1] = (char*)((*env)->GetStringUTFChars(env, old, 0));
    argv[2] = (char*)((*env)->GetStringUTFChars(env, new, 0));
    argv[3] = (char*)((*env)->GetStringUTFChars(env, patch, 0));
    //此处updatePatch()就是上面我们修改出的
    int result = updatePatch(4, argv);
    (*env)->ReleaseStringUTFChars(env, old, argv[1]);
    (*env)->ReleaseStringUTFChars(env, new, argv[2]);
    (*env)->ReleaseStringUTFChars(env, patch, argv[3]);

    return result;
}
```
接下来我们编译项目就可以了

## 合并差分包

我们假设我们已经下载了差分包（生产环境需要对下载的差分包做MD5校验），原来的APK直接在本地提取就好了。
我们安装的应用通常在、data/app下，可以通过一下代码获取其路径：
```java
context.getPackageManager().getApplicationInfo(packageName, 0).sourceDir 
```
然后可以通过PatchUtil.patchUpdate(String oldApkPath,String newApkPath,String pathPath)来进行合并了。此处需要注意:合并的过程比较耗时，需要放到子线程中进行。

## 安装
需要注意的是：在合并完成后首先要做的就是对合并的APK进行MD5校验，校验通过之后，再进行安装：
```java
  /**
     * 安装Apk
     *
     * @param context
     * @param apkPath
     */
    public static void installApk(Context context, String apkPath) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apkPath),
                "application/vnd.android.package-archive");

        context.startActivity(intent);
    }
```
到现在，增量更新已经完成。现在可以把增量包以及合并之后的安装包进行删除了。

## 示例
demo已经上传到Github：




#include "com_apkdv_smartupdate_PatchUtil.h"
#include <android/log.h>

JNIEXPORT jint JNICALL Java_com_apkdv_smartupdate_PatchUtil_patchUpdate
        (JNIEnv *env, jobject jclass1,
         jstring old, jstring new,
         jstring patch){
    char *argv[4];
    argv[0] = "bspatch";
    argv[1] = (char *) ((*env)->GetStringUTFChars(env, old, 0));
    argv[2] = (char *) ((*env)->GetStringUTFChars(env, new, 0));
    argv[3] = (char *) ((*env)->GetStringUTFChars(env, patch, 0));
    //此处updatePatch()就是上面我们修改出的
    int result = updatePatch(4, argv);
    __android_log_print(ANDROID_LOG_INFO, "ApkPatch", "applypatch result = %d ", result);
    (*env)->ReleaseStringUTFChars(env, old, argv[1]);
    (*env)->ReleaseStringUTFChars(env, new, argv[2]);
    (*env)->ReleaseStringUTFChars(env, patch, argv[3]);

    return result;
}

package com.apkdv.smartupdate;

/**
 * Created by lengyue on 2016/10/29.
 */
public class PatchUtil {
    static {
        System.loadLibrary("ApkPatch");
    }
    public static native int  patchUpdate(String oldApkPath, String newApkPath,
                                          String patchPath);
}
